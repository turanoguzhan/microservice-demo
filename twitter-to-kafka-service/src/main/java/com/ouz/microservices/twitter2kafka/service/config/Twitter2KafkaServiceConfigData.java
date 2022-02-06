package com.ouz.microservices.twitter2kafka.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data // data class tanimi, getter, setter, hashCode and toString generate edilir.
@Configuration // config bean oldugunu belirtir.
@ConfigurationProperties(prefix = "twitter-to-kafka-service") // belirtilen kaynaktaki config data propertylere erisim saglar.
public class Twitter2KafkaServiceConfigData {


    private List<String> twitterKeywords; // belirtilen config property ismi ile ayni olmak zorunda.

    private String welcomeMessage;
    private boolean enableMockTweets;
    private int mockMinTweetLength;
    private int mockMaxTweetLength;
    private long sleepMs;
}
