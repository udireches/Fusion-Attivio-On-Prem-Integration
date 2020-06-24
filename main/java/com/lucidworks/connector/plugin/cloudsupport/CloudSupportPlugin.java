package com.lucidworks.connector.plugin.cloudsupport;

import com.lucidworks.connector.plugin.cloudsupport.config.CloudSupportConfig;
import com.lucidworks.connector.plugin.cloudsupport.fetcher.SQSFetcher;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;

/**
 * A connector that ingests Attivio documents stored in S3 triggered bu AWS SQS requests
 */
public class CloudSupportPlugin implements ConnectorPluginProvider {

    @Override
    public ConnectorPlugin get() {

        return ConnectorPlugin.builder(CloudSupportConfig.class)
                .withFetcher("content", SQSFetcher.class)
                .build();
    }
}