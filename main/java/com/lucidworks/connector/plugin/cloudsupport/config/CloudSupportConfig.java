package com.lucidworks.connector.plugin.cloudsupport.config;

import com.lucidworks.fusion.connector.plugin.api.config.ConnectorConfig;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorPluginProperties;
import com.lucidworks.fusion.schema.SchemaAnnotations;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;
import com.lucidworks.fusion.schema.UIHints;

@RootSchema(
        title = "Cloud Support (v2)",
        description = "On-Prem cloud support",
        category = "Generator"
)
public interface CloudSupportConfig extends ConnectorConfig<CloudSupportConfig.Properties> {

    @Property(
            title = "Properties",
            required = true
    )
    Properties properties();

    /**
     * Connector specific settings
     */
    interface Properties extends ConnectorPluginProperties {


        @Property(
                title = "AWS Client key",
                order = 1,
                required = true,
                description = "Client key with AWS s3 and sqs access permissions"
        )
        @SchemaAnnotations.StringSchema
        String getAccessKeyId();

        @Property(
                title = "AWS Secret Key",
                order = 2,
                required = true,
                hints = {UIHints.SECRET},
                description = "Client secret key"

        )
        @SchemaAnnotations.StringSchema
        String getSecretKey();


        @Property(
                title = "On-prem Connector Name",
                order = 0,
                required = true,
                description = "The name of the on-prem connector"
        )
        @SchemaAnnotations.StringSchema
        String getOnpremConnectorName();

        @Property(
                title = "AWS Region",
                order = 3,
                required = true,
                description = "AWS Region"
        )
        @SchemaAnnotations.StringSchema
        String getAWSRegion();

        @Property(
                title = "AWS Bucket",
                order = 4,
                required = true,
                description = "AWS Bucket"
        )
        @SchemaAnnotations.StringSchema
        String getAWSBucket();
    }
}
