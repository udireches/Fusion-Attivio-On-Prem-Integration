package com.lucidworks.connector.plugin.cloudsupport.fetcher.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;

public class CredsProvider implements AWSCredentialsProvider {
    private String accessKeyId;
    private String secretKey;

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public AWSCredentials getCredentials() {
        return new Credentials(accessKeyId, secretKey);
    }

    @Override
    public void refresh() {

    }

}
