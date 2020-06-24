package com.lucidworks.connector.plugin.cloudsupport.fetcher.aws;

public class AwsConfiguration {
    private String accessKeyId;
    private String secretKey;
    private String region;
    private String s3BucketName;
    private String connectorName; //Also use as the SQS control queue name
    private String uploadQretentionPeriodSeconds;

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getS3BucketName() {
        return s3BucketName;
    }

    public void setS3BucketName(String s3BucketName) {
        this.s3BucketName = s3BucketName;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connector) {
        this.connectorName = connector;
    }

    public String getUploadQretentionPeriodSeconds() {
        return uploadQretentionPeriodSeconds;
    }

    public void setUploadQretentionPeriodSeconds(String uploadQretentionPeriodSeconds) {
        this.uploadQretentionPeriodSeconds = uploadQretentionPeriodSeconds;
    }

    @Override
    public String toString() {
        return "AwsConfiguration{" +
                "accessKeyId='" + accessKeyId + '\'' +
                ", region='" + region + '\'' +
                ", s3BucketName='" + s3BucketName + '\'' +
                ", queueName='" + connectorName + '\'' +
                ", uploadQretentionPeriodSeconds='" + uploadQretentionPeriodSeconds + '\'' +
                '}';
    }
}
