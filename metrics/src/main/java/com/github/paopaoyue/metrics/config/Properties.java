package com.github.paopaoyue.metrics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "sts-service.metrics")
public class Properties {
    private String clickhouseHost;
    private int clickhousePort;
    private String clickhouseUsername;
    private String clickhousePassword;
    private String clickhouseDatabase;

    private int insertBatchSize = 1;
    private int cardPickStatQueryTimeout = 3;  // 3 second
    private int cardPickStatRedisExpire = 60;  // 1 minutes

    public String getClickhouseHost() {
        return clickhouseHost;
    }

    public void setClickhouseHost(String clickhouseHost) {
        this.clickhouseHost = clickhouseHost;
    }

    public int getClickhousePort() {
        return clickhousePort;
    }

    public void setClickhousePort(int clickhousePort) {
        this.clickhousePort = clickhousePort;
    }

    public String getClickhouseUsername() {
        return clickhouseUsername;
    }

    public void setClickhouseUsername(String clickhouseUsername) {
        this.clickhouseUsername = clickhouseUsername;
    }

    public String getClickhousePassword() {
        return clickhousePassword;
    }

    public void setClickhousePassword(String clickhousePassword) {
        this.clickhousePassword = clickhousePassword;
    }

    public String getClickhouseDatabase() {
        return clickhouseDatabase;
    }

    public void setClickhouseDatabase(String clickhouseDatabase) {
        this.clickhouseDatabase = clickhouseDatabase;
    }

    public int getInsertBatchSize() {
        return insertBatchSize;
    }

    public void setInsertBatchSize(int insertBatchSize) {
        this.insertBatchSize = insertBatchSize;
    }

    public int getCardPickStatQueryTimeout() {
        return cardPickStatQueryTimeout;
    }

    public void setCardPickStatQueryTimeout(int cardPickStatQueryTimeout) {
        this.cardPickStatQueryTimeout = cardPickStatQueryTimeout;
    }

    public int getCardPickStatRedisExpire() {
        return cardPickStatRedisExpire;
    }

    public void setCardPickStatRedisExpire(int cardPickStatRedisExpire) {
        this.cardPickStatRedisExpire = cardPickStatRedisExpire;
    }

    @Override
    public String toString() {
        return "Properties{" +
                "insertBatchSize=" + insertBatchSize +
                ", cardPickStatQueryTimeout=" + cardPickStatQueryTimeout +
                ", cardPickStatRedisExpire=" + cardPickStatRedisExpire +
                '}';
    }
}
