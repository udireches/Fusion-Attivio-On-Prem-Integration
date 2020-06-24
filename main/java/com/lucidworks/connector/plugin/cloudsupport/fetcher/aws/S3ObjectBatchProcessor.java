package com.lucidworks.connector.plugin.cloudsupport.fetcher.aws;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.attivio.sdk.ingest.Commit;
import com.attivio.sdk.ingest.DocumentList;
import com.lucidworks.connector.plugin.cloudsupport.attiviodoc.CloudStoreReader;
import com.lucidworks.connector.plugin.cloudsupport.attiviodoc.DocumentHandler;
import com.lucidworks.connector.plugin.cloudsupport.controlmessage.ConnectorExecuted;
import com.lucidworks.connector.plugin.cloudsupport.util.MiscUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Process a batch of messages from the on-prem connector. The S3 object information is obtained from the SQS {@link ConnectorExecuted} message.
 */
public class S3ObjectBatchProcessor {
    private final Logger logger;
    private final AwsClientFactory awsFactory;

    public S3ObjectBatchProcessor(AwsClientFactory awsFactory) {
        this.awsFactory = awsFactory;
        logger = LoggerFactory.getLogger(MiscUtils.loggerName(S3ObjectBatchProcessor.class, awsFactory.getConnectorName()));
    }

    public void process(DocumentHandler documentHandler, ConnectorExecuted connectorExecuted) {
        process(documentHandler, connectorExecuted.getBucketName(), connectorExecuted.getObjectKey());
    }

    private void process(DocumentHandler documentHandler, String bucketName, String objectKey) {

        AmazonS3 s3Client = awsFactory.getAmazonS3();
        long counter = 0;
        try (ResilientS3Stream s3ObjectStream =
                     new ResilientS3Stream(
                             s3Client,
                             new GetObjectRequest(bucketName, objectKey))) {
            Object msgObj;

            try (CloudStoreReader reader = new CloudStoreReader(s3ObjectStream)) {
                while ((msgObj = nextMessage(reader)) != null) {

                    if (msgObj instanceof DocumentList) {
                        DocumentList docList = (DocumentList) msgObj;
                        logger.info("Processing {} documents", docList.size());
                        documentHandler.handle(docList);
                        logger.info("Done processing {} documents", docList.size());

                    } else if (msgObj instanceof Commit) {
                        logger.info("Processing a commit message");
                        documentHandler.handle((Commit) msgObj);
                    } else {
                        logger.error(String.format("Unknown class %s", msgObj.getClass()));

                    }
                    counter++;

                }
            } catch (IOException e) {
                logObjectReadFailure(e, bucketName, objectKey);

            }
        } catch (SdkClientException | IOException e) {
            logObjectReadFailure(e, bucketName, objectKey);

        }
        logger.info(String.format("Handled %s objects from  %s:%s ", counter, bucketName, objectKey));

    }

    private void logObjectReadFailure(Throwable t, String bucketName, String objectKey) {
        logger.error(String.format("Failed to read S3 object %s:%s", bucketName, objectKey), t);
    }

    private Object nextMessage(CloudStoreReader reader) throws IOException {
        Object message = null;
        boolean found = false;
        while (!found) {
            try {
                message = reader.readObject();
                found = true;
            } catch (ClassNotFoundException e) {

                logger.error("Message object class not found", e);
                e.printStackTrace();
            }
        }
        return message;
    }

}
