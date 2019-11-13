package com.nilepoint.monitorevaluatemobile.persistence;

import java.io.Serializable;

/**
 * Created by ashaw on 8/24/17.
 *
 * This class will allow user to select their environment
 */

public class Environment implements Serializable {
    String name;

    String amqpHostname;
    String amqpUsername;
    String amqpPassword;

    String exchangeHostname;
    String exchangeKey;

    String apiHostname;
    String authHostname;

    public Environment() {
    }

    public Environment(String name,
                       String amqpHostname,
                       String amqpUsername,
                       String amqpPassword,
                       String exchangeHostname,
                       String exchangeKey,
                       String authHostname,
                       String apiHostname) {
        this.name = name;
        this.amqpHostname = amqpHostname;
        this.amqpUsername = amqpUsername;
        this.amqpPassword = amqpPassword;
        this.exchangeHostname = exchangeHostname;
        this.exchangeKey = exchangeKey;
        this.apiHostname = apiHostname;
        this.authHostname = authHostname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmqpHostname() {
        return amqpHostname;
    }

    public void setAmqpHostname(String amqpHostname) {
        this.amqpHostname = amqpHostname;
    }

    public String getExchangeHostname() {
        return exchangeHostname;
    }

    public void setExchangeHostname(String exchangeHostname) {
        this.exchangeHostname = exchangeHostname;
    }

    public String getExchangeKey() {
        return exchangeKey;
    }

    public void setExchangeKey(String exchangeKey) {
        this.exchangeKey = exchangeKey;
    }

    public String getAmqpUsername() {
        return amqpUsername;
    }

    public void setAmqpUsername(String amqpUsername) {
        this.amqpUsername = amqpUsername;
    }

    public String getAmqpPassword() {
        return amqpPassword;
    }

    public void setAmqpPassword(String amqpPassword) {
        this.amqpPassword = amqpPassword;
    }

    public String getApiHostname() {
        return apiHostname;
    }

    public void setApiHostname(String apiHostname) {
        this.apiHostname = apiHostname;
    }

    public String getAuthHostname() {
        return authHostname;
    }

    public void setAuthHostname(String authHostname) {
        this.authHostname = authHostname;
    }
}
