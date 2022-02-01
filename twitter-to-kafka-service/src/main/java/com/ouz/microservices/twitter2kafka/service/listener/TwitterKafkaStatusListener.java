package com.ouz.microservices.twitter2kafka.service.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.StatusAdapter;

/**
 * @Component, @Service, @Controller, @Repository, @Configuration
 * Yukarıdaki annotationlar Spring Bean tarafından runtime de otomatik olarak
 * taranır (scanned) ve yüklenirler (loaded).
 */
@Component
public class TwitterKafkaStatusListener extends StatusAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterKafkaStatusListener.class);

    @Override
    public void onStatus(Status status) {
        super.onStatus(status);
        LOG.info("Twitter status => {}",status);

    }
}
