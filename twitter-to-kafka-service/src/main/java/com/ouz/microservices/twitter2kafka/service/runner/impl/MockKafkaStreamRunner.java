package com.ouz.microservices.twitter2kafka.service.runner.impl;

import com.ouz.microservices.twitter2kafka.service.config.Twitter2KafkaServiceConfigData;
import com.ouz.microservices.twitter2kafka.service.exception.TwitterToKafkaServiceException;
import com.ouz.microservices.twitter2kafka.service.listener.TwitterKafkaStatusListener;
import com.ouz.microservices.twitter2kafka.service.runner.TwitterStreamKafkaRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;


@Component
@ConditionalOnProperty(value="twitter-to-kafka-service.enable-mock-tweets",havingValue = "true")
public class MockKafkaStreamRunner implements TwitterStreamKafkaRunner {

    private static final Logger LOG = LoggerFactory.getLogger(MockKafkaStreamRunner.class);

    private final Twitter2KafkaServiceConfigData twitter2KafkaServiceConfigData;
    private final TwitterKafkaStatusListener twitterKafkaStatusListener;

    private static final Random RANDOM = new Random();

    private static final String[] MOCK_WORDS = new String[]{
            "Lorem",
            "ipsum",
            "dolor",
            "sit",
            "dolor",
            "amet",
            "consectetur",
            "adipiscing",
            "elit",
            "Maecenas",
            "id",
            "sapien",
            "sit",
            "amet",
            "lacus",
            "elementum",
            "pretium",
            "eu",
            "at",
            "eros"
    };

    private static final String tweetStringJsonRaw="{" +
            "\"created_at\":\"{0}\","+
            "\"id\":\"{1}\","+
            "\"text\":\"{2}\","+
            "\"user\":{\"id\":\"{3}\"}"+
            "}";

    private static final String TWITTER_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";

    public MockKafkaStreamRunner(Twitter2KafkaServiceConfigData twitter2KafkaServiceConfigData, TwitterKafkaStatusListener twitterKafkaStatusListener) {
        this.twitter2KafkaServiceConfigData = twitter2KafkaServiceConfigData;
        this.twitterKafkaStatusListener = twitterKafkaStatusListener;
    }

    @Override
    public void start() {
        String[] keywords = twitter2KafkaServiceConfigData.getTwitterKeywords().toArray(new String[0]);
        int minTweetLength=twitter2KafkaServiceConfigData.getMockMinTweetLength();
        int maxTweetLength=twitter2KafkaServiceConfigData.getMockMaxTweetLength();
        long sleepMs = twitter2KafkaServiceConfigData.getSleepMs();
        LOG.info("Starting mock streaming tweets with keywords => ", Arrays.toString(keywords));

        simulateTweet(keywords, minTweetLength, maxTweetLength, sleepMs);
    }

    private void simulateTweet(String[] keywords, int minTweetLength, int maxTweetLength, long sleepMs) {
        // we don't want to interrupt the main thread so we run tweet on a single thread with help executor
        try {
            Executors.newSingleThreadExecutor().submit(()->{
                while(true){
                    String formattedTextAsJson = getFormattedTextAsJson(keywords, minTweetLength, maxTweetLength);
                    Status status = TwitterObjectFactory.createStatus(formattedTextAsJson);
                    twitterKafkaStatusListener.onStatus(status);
                    sleep(sleepMs);
                }
            });
        } catch (Exception e) {
            LOG.error("Error pop up while tweet streaming , "+e.getLocalizedMessage());
        }
    }

    private void sleep(long millis){
        try{
            Thread.sleep(millis);
        }catch(RuntimeException | InterruptedException ex){
            throw new TwitterToKafkaServiceException("error encountered while running tweet mocking ,"+ex.getLocalizedMessage());
        }
    }

    private String getFormattedTextAsJson(String[] keywords, int minTweetLength, int maxTweetLength) {

        String[] params=new String[]{
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(TWITTER_DATE_FORMAT, Locale.ENGLISH)),
                String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)),
                getRandomTweetContent(keywords,minTweetLength,maxTweetLength),
                String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE))
        };

        return formatTextAsJsonWithParams(params);

    }

    private String formatTextAsJsonWithParams(String[] params) {
        String tweet = tweetStringJsonRaw;
        for(int i = 0; i< params.length; i++){
            tweet = tweet.replace("{"+i+"}", params[i]);
        }
        return tweet;
    }

    private String getRandomTweetContent(String[] keywords, int minTweetLength, int maxTweetLength){

        int tweetLength = RANDOM.nextInt(maxTweetLength-minTweetLength+1)+minTweetLength;

        return constructRandomTweetContent(keywords, tweetLength);
    }

    private String constructRandomTweetContent(String[] keywords, int tweetLength) {

        StringBuilder tweetBuilder = new StringBuilder();

        for(int i = 0; i< tweetLength; i++){
            tweetBuilder.append(MOCK_WORDS[RANDOM.nextInt(MOCK_WORDS.length)]).append(" ");

            if(i == tweetLength /2){
                tweetBuilder.append(keywords[RANDOM.nextInt(keywords.length)]).append(" ");
            }
        }
        return tweetBuilder.toString().trim();
    }
}
