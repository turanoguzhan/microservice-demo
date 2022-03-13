package com.ouz.microservices.kafka.admin.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * custom exception class for seperating error types and messages.
 */
public class KafkaClientException extends RuntimeException{

    private final Logger LOG = LoggerFactory.getLogger(KafkaClientException.class);

    public KafkaClientException(String msg) {
        LOG.info("Exception is created : {}",msg);
    }

    public KafkaClientException(String msg, Throwable t) {
        LOG.info("Exception is created : {},  Throwable message : {}",msg,t);
    }
}
