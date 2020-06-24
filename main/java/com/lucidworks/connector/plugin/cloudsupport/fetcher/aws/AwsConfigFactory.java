package com.lucidworks.connector.plugin.cloudsupport.fetcher.aws;

import com.lucidworks.connector.plugin.cloudsupport.config.CloudSupportConfig;

public class AwsConfigFactory {
    public static AwsConfiguration getConfiguration() {
        return getConfiguration(null);
    }

    public static AwsConfiguration getConfiguration(CloudSupportConfig configuredProps) {
        AwsConfiguration awsConfiguration = new AwsConfiguration();

        //Defaults
        awsConfiguration.setAccessKeyId("xxxxxxx");
        awsConfiguration.setRegion("us-east-2");
        awsConfiguration.setS3BucketName("udi.test.attivio.onprem");
        awsConfiguration.setConnectorName("testConnector");
        awsConfiguration.setUploadQretentionPeriodSeconds("3600");
        awsConfiguration.setSecretKey("yyyyyyy");

        if (configuredProps == null) {
            return awsConfiguration;
        }

        awsConfiguration.setAccessKeyId(configuredProps.properties().getAccessKeyId());
        awsConfiguration.setRegion(configuredProps.properties().getAWSRegion());
        awsConfiguration.setSecretKey(configuredProps.properties().getSecretKey());
        awsConfiguration.setConnectorName(configuredProps.properties().getOnpremConnectorName());
        awsConfiguration.setS3BucketName(configuredProps.properties().getAWSBucket());

        return awsConfiguration;
    }
}
