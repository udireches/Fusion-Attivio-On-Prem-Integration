package com.lucidworks.connector.plugin.cloudsupport.attiviodoc;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.SSECustomerKey;
import com.attivio.sdk.ingest.ContentPointer;
import com.google.common.annotations.VisibleForTesting;
import com.lucidworks.connector.plugin.cloudsupport.fetcher.aws.ResilientS3Stream;

import javax.crypto.SecretKey;
import java.io.InputStream;

/**
 * Specifies where the document's content is stored.
 */
public class S3ContentPointer implements ContentPointer {
    private static final int MAX_STREAM_RETRY = 3;
    private static final String NOT_FOUND_ERROR = "404 Not Found";
    private static final String NO_SUCH_KEY_ERROR = "NoSuchKey";

    final String region;
    final String bucketName;
    final String objectKey;
    final SecretKey encryptionKey;
    private AmazonS3 s3;

    public S3ContentPointer(String region, String bucketName, String objectKey, SecretKey encryptionKey) {
        this.region = region;
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.encryptionKey = encryptionKey;
    }

    private S3ContentPointer(AmazonS3 s3, String region, String bucketName, String objectKey, SecretKey encryptionKey) {
        this(region, bucketName, objectKey, encryptionKey);
        this.s3 = s3;

    }

    @Override
    public String toString() {
        return "S3ContentPointer{" +
                "region='" + region + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", objectKey='" + objectKey + '\'' +
                '}';
    }

    public static S3ContentPointer S3CPFromSDKCP(AmazonS3 s3, ContentPointer sdkCP, String region) {
        String[] bucketAndObjectId = sdkCP.getId().split("@");
        return new S3ContentPointer(s3, region, bucketAndObjectId[1], bucketAndObjectId[0], null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getExternalUri() {
        return s3.getUrl(bucketName, objectKey).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return objectKey + "@" + bucketName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLastModified() {
        try {
            ObjectMetadata om = s3.getObjectMetadata(bucketName, objectKey);
            return om.getLastModified().getTime();
        } catch (AmazonS3Exception ex) {
            if (NOT_FOUND_ERROR.equals(ex.getErrorCode())) {
                return -1;
            }
            throw ex;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSize() {
        try {
            ObjectMetadata om = s3.getObjectMetadata(bucketName, objectKey);
            return om.getContentLength();
        } catch (AmazonS3Exception ex) {
            if (NOT_FOUND_ERROR.equals(ex.getErrorCode())) {
                return -1;
            }
            throw ex;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStoreName() {
        return bucketName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getStream() {
        try {
            GetObjectRequest req = new GetObjectRequest(bucketName, objectKey);
            if (encryptionKey != null) {
                req.withSSECustomerKey(new SSECustomerKey(encryptionKey));
            }
            return new ResilientS3Stream(s3, req).withMaxRetries(MAX_STREAM_RETRY);

        } catch (AmazonS3Exception ex) {
            if (NO_SUCH_KEY_ERROR.equals(ex.getErrorCode())) {
                return null;
            }
            throw ex;
        }
    }

    @VisibleForTesting
    public boolean isEncrypted() {
        return encryptionKey != null;
    }


}
