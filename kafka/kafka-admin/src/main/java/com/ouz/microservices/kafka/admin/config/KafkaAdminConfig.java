package com.ouz.microservices.kafka.admin.config;

import com.ouz.microservices.config.KafkaConfigData;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import java.util.Map;

@EnableRetry
@Configuration
public class KafkaAdminConfig {

    private final KafkaConfigData kafkaConfigData;

    public KafkaAdminConfig(KafkaConfigData kafkaConfigData){
        this.kafkaConfigData = kafkaConfigData;
    }

    /**
     * manage and inspect brokers, topic and configuratins
     * @return
     */
    @Bean
    public AdminClient adminClient(){
        adminClient().create(Map.of(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
                kafkaConfigData.getBootstrapServers()));
    }
}
