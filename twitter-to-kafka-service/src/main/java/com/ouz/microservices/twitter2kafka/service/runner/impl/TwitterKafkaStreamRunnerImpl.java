package com.ouz.microservices.twitter2kafka.service.runner.impl;

import com.ouz.microservices.config.Twitter2KafkaServiceConfigData;
import com.ouz.microservices.twitter2kafka.service.listener.TwitterKafkaStatusListener;
import com.ouz.microservices.twitter2kafka.service.runner.TwitterStreamKafkaRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import twitter4j.FilterQuery;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import javax.annotation.PreDestroy;

/**
 * Spring bean'ler default olarak Singleton scope da oluşturulurlar.
 * Singleton => nesne bir kez oluşturulur ve obje inject edildiğinde aynı bean kullanılır.
 * Prototype => Her nesne çağırımında yeni bir nesne oluşturulur.
 *
 * ConditionalOnProperty => property lerin duruma göre inject edilmesini
 * bean oluşturma işleminin property durumuna bagli olarak değiştirilmek istendiğinde kullanılır.
 */

@Service
@ConditionalOnProperty(value="twitter-to-kafka-service.enable-mock-tweets",havingValue = "false",matchIfMissing = true)
@Scope(value="Prototype")
public class TwitterKafkaStreamRunnerImpl implements TwitterStreamKafkaRunner {

    private final Logger LOG = LoggerFactory.getLogger(TwitterKafkaStreamRunnerImpl.class);

    private final Twitter2KafkaServiceConfigData twitter2KafkaServiceConfigData;
    private final TwitterKafkaStatusListener twitterKafkaStatusListener;

    private TwitterStream twitterStream;

    public TwitterKafkaStreamRunnerImpl(Twitter2KafkaServiceConfigData configData,
                                        TwitterKafkaStatusListener statusListener) {
        this.twitter2KafkaServiceConfigData = configData;
        this.twitterKafkaStatusListener = statusListener;
    }

    @Override
    public void start() throws TwitterException {
        twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(twitterKafkaStatusListener);
        addFilter();
    }

    private void addFilter() {
        String[] keywords = twitter2KafkaServiceConfigData.getTwitterKeywords().toArray(new String[0]);
        FilterQuery filterQuery = new FilterQuery(keywords);
        twitterStream.filter(filterQuery);
        twitterStream.filter(filterQuery.language("TR","tr"));
        LOG.info("Starting streaming for Twitter keywords " + keywords);
    }

    /**
     * kaynakların serbest bırakılması için işimiz bittğinde otomatik olarak
     * Spring tarafından çalıştırılır.
     * PreDestroy annotation @Prototype olarak işaretlenmiş scope da çalışmaz.
     */
    @PreDestroy
    public void shutdown(){
        if(twitterStream != null){
            LOG.info("Closing twitter streaming !");
            twitterStream.shutdown();
        }
    }
}
