package com.lucidworks.connector.plugin.cloudsupport.fetcher.aws;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.lucidworks.connector.plugin.cloudsupport.util.MiscUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Receives SQS messages in a thread and passes them to handler. The thread terminates when there are no more messages.
 */
public class SQSMessageReceiver {
    private AwsConfiguration awsConfiguration;
    private AmazonSQS amazonSQS;
    private String qUrl;
    private volatile boolean stop = false;
    private ControlMessageHandler handler;
    private Logger logger;

    public void init(AwsConfiguration awsConfiguration, AwsClientFactory clientfactory) {

        this.awsConfiguration = awsConfiguration;

        amazonSQS = clientfactory.getAmazonSQS();

        logger = LoggerFactory.getLogger(MiscUtils.loggerName(SQSMessageReceiver.class, awsConfiguration.getConnectorName()));

    }


    public void startReceivingMessages(ControlMessageHandler handler) {
        this.handler = handler;
        this.qUrl = amazonSQS.getQueueUrl(awsConfiguration.getConnectorName()).getQueueUrl();
        startReceiving();
    }

    private void startReceiving() {
        logger.info("Starting the message receiver thread");
        Thread receiver = new Thread(this.getClass().getName()) {
            @Override
            public void run() {
                while (!stop) {
                    receiveMessages();
                }
                amazonSQS.shutdown();
            }
        };
        receiver.setDaemon(true);
        receiver.start();
    }

    private void receiveMessages() {

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(qUrl).withMessageAttributeNames(".*");

        receiveMessageRequest.setWaitTimeSeconds(1);

        List<Message> messages = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();
        if (messages == null || messages.size() == 0) {
            logger.info("No new messages - terminating");
            handler.done();
            stopReceivingMessages();
            return;
        }
        for (final Message message : messages) {
            logger.info("Received {} messages", messages.size());
            handleMessage(message);
            amazonSQS.deleteMessage(qUrl, message.getReceiptHandle());

        }

    }

    private void handleMessage(Message message) {
        //A place holder for future use
        message.getMessageAttributes().get(ControlMessageHandler.UNUSED).getStringValue();

        String docContent = message.getBody();
        handler.handleMessage(docContent, message.getMessageAttributes());

    }


    public void stopReceivingMessages() {
        stop = true;
    }


}
