package com.ouz.microservices.twitter2kafka.service.runner;

import com.ouz.microservices.twitter2kafka.service.config.Twitter2KafkaServiceConfigData;
import com.ouz.microservices.twitter2kafka.service.listener.TwitterKafkaStatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import twitter4j.FilterQuery;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import javax.annotation.PreDestroy;

/**
 * Spring bean'ler default olarak Singleton scope da oluşturulurlar.
 * Singleton => nesne bir kez oluşturulur ve obje inject edildiğinde aynı bean kullanılır.
 * Prototype => Her nesne çağırımında yeni bir nesne oluşturulur
 */

@Service
public class TwitterKafkaStreamRunnerImpl implements TwitterStreamKafkaRunner{

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
        LOG.info("Starting streaming for Twitter keywords {}",keywords);
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
