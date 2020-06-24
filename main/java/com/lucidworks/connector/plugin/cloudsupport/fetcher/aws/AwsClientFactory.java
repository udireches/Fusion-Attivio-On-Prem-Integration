package com.lucidworks.connector.plugin.cloudsupport.fetcher.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;


public class AwsClientFactory {

    private final AwsConfiguration awsConfiguration;
    private final AmazonSQS amazonSQS;
    private final AmazonS3 amazonS3;

    public AwsClientFactory(AwsConfiguration awsConfiguration) {
        super();
        this.awsConfiguration = awsConfiguration;
        amazonSQS = createSQSClient();
        amazonS3 = createS3Client();
    }

    public AmazonSQS getAmazonSQS() {
        return amazonSQS;
    }

    public AmazonS3 getAmazonS3() {
        return amazonS3;
    }

    public String getConnectorName() {
        return awsConfiguration.getConnectorName();
    }

    private AmazonSQS createSQSClient() {

        AmazonSQSClientBuilder builder = AmazonSQSClientBuilder.standard().withCredentials(createCredsProvider());
        return builder.withRegion(awsConfiguration.getRegion()).build();
    }

    private AmazonS3 createS3Client() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().withCredentials(createCredsProvider());
        return builder.withRegion(awsConfiguration.getRegion()).build();
    }

    private CredsProvider createCredsProvider() {
        CredsProvider credsProvider = new CredsProvider();
        credsProvider.setAccessKeyId(awsConfiguration.getAccessKeyId());
        credsProvider.setSecretKey(awsConfiguration.getSecretKey());
        return credsProvider;
    }
}
