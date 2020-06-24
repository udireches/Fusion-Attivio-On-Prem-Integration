package com.lucidworks.connector.plugin.cloudsupport.fetcher.aws;

import com.amazonaws.auth.AWSCredentials;

class Credentials implements AWSCredentials {
    private String accessKeyId;
    private String secretKey;

    public Credentials(String accessKeyId, String secretKey) {
        super();
        this.accessKeyId = accessKeyId;
        this.secretKey = secretKey;
    }

    @Override
    public String getAWSAccessKeyId() {
        return accessKeyId;
    }

    @Override
    public String getAWSSecretKey() {
        return secretKey;

    }
}