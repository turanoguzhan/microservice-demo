package com.ouz.microservices.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

//@Data annotation => data class tanimi, getter, setter, hashCode and toString generate edilir.
//@Configuration annotation => config bean oldugunu belirtir.
//@ConfigurationProperties annotation => belirtilen kaynaktaki config data propertylere erisim saglar.
@Data
@Configuration
@ConfigurationProperties(prefix = "twitter-to-kafka-service")
public class Twitter2KafkaServiceConfigData {

    // belirtilen config property ismi ile ayni olmak zorunda.
    private List<String> twitterKeywords;
    private String welcomeMessage;
    private Boolean enableMockTweets;
    private Long mockSleepMs;
    private Integer mockMinTweetLength;
    private Integer mockMaxTweetLength;
}
