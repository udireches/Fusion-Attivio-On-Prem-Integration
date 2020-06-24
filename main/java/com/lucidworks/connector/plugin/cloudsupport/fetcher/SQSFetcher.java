package com.lucidworks.connector.plugin.cloudsupport.fetcher;

import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.lucidworks.connector.plugin.cloudsupport.config.CloudSupportConfig;
import com.lucidworks.connector.plugin.cloudsupport.controlmessage.CloudMessage;
import com.lucidworks.connector.plugin.cloudsupport.controlmessage.ConnectorExecuted;
import com.lucidworks.connector.plugin.cloudsupport.fetcher.aws.*;
import com.lucidworks.connector.plugin.cloudsupport.util.JsonSerializer;
import com.lucidworks.connector.plugin.cloudsupport.util.MiscUtils;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

/**
 * The goal of this connector is to ingest Attivio documents from attivio on-premises connectors. The assumption is that the
 * on-prem connectors don't have to be modified or reconfigured in order to integrate with Fusion.
 * <p>
 * The connector  consumes SQS messages and S3 objects from the on-premises connector.  SQS messages are used for control - they point
 * to S3 objects that contain the Attivio documents. The documents are ingested and the control messages are deleted.
 * <p>
 * The on-prem connectors use a single queue for all the control messages that go to the cloud instance. The Attivio cloud instance
 * in turn configure and start a connector instance per on-prem instance. This Fusion connector, on the other hand, uses a control
 * queue per connector, that means that messages from the single control queue must be forwarded to each  specific connector's queue. AWS
 * Lambda will be used for that.
 * <p>
 * Each instance of this connector is configured with AWS credentials and the name of the on-prem connector. It is secheduled
 * to run every few minutes to check the SQS queue. The connector run ends once there are messages on the queue.
 */
public class SQSFetcher implements ContentFetcher {

    private final Logger logger;

    private final AwsConfiguration awsConfiguration;
    private final AwsClientFactory clientfactory;
    private final S3ObjectBatchProcessor batchProcessor;

    @Inject
    public SQSFetcher(

            CloudSupportConfig cloudSupportConfig

    ) {
        awsConfiguration = AwsConfigFactory.getConfiguration(cloudSupportConfig);
        logger = LoggerFactory.getLogger(MiscUtils.loggerName(SQSFetcher.class, awsConfiguration.getConnectorName()));
        clientfactory = new AwsClientFactory(awsConfiguration);
        batchProcessor = new S3ObjectBatchProcessor(clientfactory);
    }

    @Override
    public FetchResult fetch(FetchContext fetchContext) {
        logger.info("Connector %s starts processing SQS message with config: {}", awsConfiguration.toString());
        SQSMessageReceiver messageReciver = new SQSMessageReceiver();
        messageReciver.init(awsConfiguration, clientfactory);
        MessageHandlerImpl handler = new MessageHandlerImpl(fetchContext);
        messageReciver.startReceivingMessages(handler);

        handler.waitForDone();

        return fetchContext.newResult();
    }


    private class MessageHandlerImpl implements ControlMessageHandler {
        private volatile boolean done = false;
        private final FetchContext fetchContext;

        MessageHandlerImpl(FetchContext fetchContext) {
            this.fetchContext = fetchContext;
        }

        @Override
        public void handleMessage(String messageText, Map<String, MessageAttributeValue> attributesUnused) {
            logger.info("Handling SQS message {}", messageText);
            ConnectorExecuted connectorExecuted;
            try {
                connectorExecuted = JsonSerializer.createObjectMapper().readerFor(CloudMessage.class).readValue(messageText);
            } catch (Throwable t) {
                logger.error("Failed to unmarshall SQS message", t);
                return;
            }

            fetchContext.newDocument(awsConfiguration.getConnectorName() + ".control." + connectorExecuted.getUuid())
                    .fields(f -> f.setString("messagetext_s", messageText))
                    .emit();
            batchProcessor.process(new FusionDocConverter(fetchContext, logger, awsConfiguration, clientfactory), connectorExecuted);

        }

        void waitForDone() {
            while (!done) {
                try {
                    synchronized (this) {
                        this.wait(1000);
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        @Override
        public synchronized void done() {
            done = true;
            notifyAll();
        }
    }


}
