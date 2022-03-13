package com.ouz.microservices.kafka.admin.config;

import com.ouz.microservices.config.KafkaConfigData;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import java.util.Map;

// for using retry implementation we add @enableretry annotation.

@EnableRetry
@Configuration
public class KafkaAdminConfig {

    private final KafkaConfigData kafkaConfigData;

    public KafkaAdminConfig(KafkaConfigData configData) {
        this.kafkaConfigData = configData;
    }

    /**
     * manage and inspect brokers, topic and configuratins
     * @return
     */
    @Bean
    public AdminClient adminClient(){
        return AdminClient.create(Map.of(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
                kafkaConfigData.getBootstrapServers()));
    }
}
