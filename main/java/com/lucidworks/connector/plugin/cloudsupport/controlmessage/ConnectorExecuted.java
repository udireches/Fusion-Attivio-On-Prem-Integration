package com.lucidworks.connector.plugin.cloudsupport.controlmessage;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.Date;

/**
 * This message is sent by the on-prem connector over an SQS queue to the connector in the cloud. It specifies where
 * locate this batch of Attivio documents.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@SuppressWarnings("hiding")
public class ConnectorExecuted implements CloudMessage {
    private String region;
    private String bucketName;
    private String objectKey;
    private String connectorName;
    private String uuid;
    private String targetWorkflow;
    private String encryptionKey;

    private double mb;
    private long dateUploaded;
    private long documents;

    public ConnectorExecuted withRegion(String region) {
        this.region = region;
        return this;
    }

    public ConnectorExecuted withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public ConnectorExecuted withConnectorName(String connectorName) {
        this.connectorName = connectorName;
        return this;
    }

    public ConnectorExecuted withObjectKey(String objectKey) {
        this.objectKey = objectKey;
        return this;
    }

    public ConnectorExecuted withBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public ConnectorExecuted withDateUploaded(Date dateUploaded) {
        this.dateUploaded = dateUploaded.getTime();
        return this;
    }

    public ConnectorExecuted withMb(double mb) {
        this.mb = mb;
        return this;
    }

    public ConnectorExecuted withDocuments(long documents) {
        this.documents = documents;
        return this;
    }

    public ConnectorExecuted withTargetWorkflow(String targetWorkflow) {
        this.targetWorkflow = targetWorkflow;
        return this;
    }

    public ConnectorExecuted withEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
        return this;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ConnectorExecuted [region="
                + region
                + ", bucketName="
                + bucketName
                + ", objectKey="
                + objectKey
                + ", connectorName="
                + connectorName
                + ", uuid="
                + uuid
                + "]";
    }

    public double getMb() {
        return mb;
    }

    public long getDocuments() {
        return documents;
    }

    public long getDateUploaded() {
        return dateUploaded;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public String getRegion() {
        return region;
    }

    public String getTargetWorkflow() {
        return targetWorkflow;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public String getUuid() {
        return uuid;
    }
}
