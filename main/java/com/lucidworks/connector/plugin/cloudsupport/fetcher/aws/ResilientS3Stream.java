package com.lucidworks.connector.plugin.cloudsupport.fetcher.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.lucidworks.connector.plugin.cloudsupport.util.MiscUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * An resumable S3  content stream. It recovers when the S3 streams fails - can be happen when the stream is
 * open for hours.
 */
public class ResilientS3Stream extends CountingInputStream {

    private int maxRetries = 3;
    private int retryCount = 0;
    private long lastRestart = -1;
    private final AmazonS3 s3;
    private final GetObjectRequest s3Request;
    private final Logger logger;

    public ResilientS3Stream(AmazonS3 s3, GetObjectRequest getObjectRequest) {
        super(null);
        this.s3 = s3;
        this.s3Request = getObjectRequest;
        S3Object s3Object = s3.getObject(getObjectRequest);
        this.in = s3Object.getObjectContent();
        logger = LoggerFactory.getLogger(MiscUtils.loggerName(ResilientS3Stream.class, null));
    }

    @Override
    public int read() throws IOException {
        try {
            return super.read();
        } catch (IOException e) {
            restartAt(getByteCount());
            return super.read();
        }
    }

    @Override
    public int read(byte[] b, int offset, int len) throws IOException {
        try {
            return super.read(b, offset, len);
        } catch (IOException e) {
            restartAt(getByteCount());
            return super.read(b, offset, len);
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        try {
            return super.read(b);
        } catch (IOException e) {
            restartAt(getByteCount());
            return super.read(b);
        }
    }

    public synchronized void restartAt(long offset) throws IOException {
        logger.info(String.format("Restarting stream stream %s:%s retry count %s", s3Request.getBucketName(), s3Request.getS3ObjectId().getKey(), retryCount));
        if (lastRestart == offset && (++retryCount > maxRetries)) {
            String errorMsg = String.format("Unable to resume S3Object stream %s:%s with range start %s maxRetries (%s) exceeded",
                    s3Request.getBucketName(), s3Request.getS3ObjectId().getKey(), offset, maxRetries);
            logger.error(errorMsg);
            throw new IOException(errorMsg);
        }
        lastRestart = offset;
        retryCount = 0;
        GetObjectRequest restart = (GetObjectRequest) s3Request.clone();
        restart.withRange(offset);
        S3Object restartObject = s3.getObject(restart);
        abort();
        if (restartObject == null) {
            String errorMsg = String.format("Unable to resume S3Object stream %s:%s with range start %s",
                    s3Request.getBucketName(), s3Request.getS3ObjectId().getKey(), offset);
            logger.error(errorMsg);
            throw new IOException(errorMsg);
        }

        this.in = restartObject.getObjectContent();
    }

    /**
     * Abort the stream
     */
    public void abort() {
        if (this.in instanceof S3ObjectInputStream) {
            ((S3ObjectInputStream) this.in).abort();
        }
    }

    /**
     * delegates to S3ObjectStream.release
     */
    public void release() {
        if (this.in instanceof S3ObjectInputStream) {
            ((S3ObjectInputStream) this.in).release();
        }
    }


    /**
     * Maximum number of retry attempts before aborting.
     *
     * @param maxRetries max number of retries
     * @return this stream
     */
    public ResilientS3Stream withMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }
}
