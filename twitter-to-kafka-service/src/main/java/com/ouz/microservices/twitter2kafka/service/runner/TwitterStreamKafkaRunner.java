package com.ouz.microservices.twitter2kafka.service.runner;

import twitter4j.TwitterException;

public interface TwitterStreamKafkaRunner {

    void start() throws TwitterException;
}
