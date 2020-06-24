package com.lucidworks.connector.plugin.cloudsupport.fetcher.aws;

import com.amazonaws.services.sqs.model.MessageAttributeValue;

import java.util.Map;

/**
 * The handler on on-pem control messages implements this interface
 */
public interface ControlMessageHandler {

    public static final String UNUSED = "someprop";

    /**
     * HAndle a control message from the on-prem connector
     * @param messageText
     * @param attributes
     */
    public void handleMessage(String messageText, Map<String, MessageAttributeValue> attributes);

    /**
     * There are no more control messages to process.
     */
    public void done();
}
