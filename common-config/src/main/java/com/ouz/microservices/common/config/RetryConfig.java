package com.ouz.microservices.common.config;

import com.ouz.microservices.config.RetryConfigData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryConfig {
    private RetryConfigData retryConfigData;

    public RetryConfig(RetryConfigData configData) {
        this.retryConfigData = configData;
    }

    @Bean
    public RetryTemplate retryTemplate(){

        RetryTemplate retryTemplate = new RetryTemplate();

        /**
         * ExponentialBackOffPolicy helps us increasing waiting time for each retry attempt
         */
        ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
        exponentialBackOffPolicy.setInitialInterval(retryConfigData.getInitialIntervalMs());
        exponentialBackOffPolicy.setMaxInterval(retryConfigData.getMaxIntervalMs());
        exponentialBackOffPolicy.setMultiplier(retryConfigData.getMultiplier());

        retryTemplate.setBackOffPolicy(exponentialBackOffPolicy);

        /**
         * retry until max attempt reached.
         */
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(retryConfigData.getMaxAttempts());

        retryTemplate.setRetryPolicy(simpleRetryPolicy);

        return retryTemplate;
    }

}
