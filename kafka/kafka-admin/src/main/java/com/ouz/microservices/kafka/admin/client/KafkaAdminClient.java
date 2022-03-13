package com.ouz.microservices.kafka.admin.client;

import com.ouz.microservices.config.KafkaConfigData;
import com.ouz.microservices.config.RetryConfigData;
import com.ouz.microservices.kafka.admin.exception.KafkaClientException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Component
public class KafkaAdminClient {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaAdminClient.class);

    private final KafkaConfigData kafkaConfigData;
    private final RetryConfigData retryConfigData;
    private final AdminClient adminClient;
    private final WebClient webClient;
    private final RetryTemplate retryTemplate;

    public KafkaAdminClient(KafkaConfigData kafkaConfigData, RetryConfigData retryConfigData,
                            AdminClient adminClient, WebClient webClient, RetryTemplate retryTemplate) {
        this.kafkaConfigData = kafkaConfigData;
        this.retryConfigData = retryConfigData;
        this.adminClient = adminClient;
        this.webClient = webClient;
        this.retryTemplate = retryTemplate;
    }

    public void createTopic(){
        CreateTopicsResult createTopicsResult;

        try {
            createTopicsResult = retryTemplate.execute(this::doCreateTopic);
        }catch(Throwable t){
            throw new KafkaClientException("Reached the max number of retry to creating kafka topic(s).",t);
        }
        checkTopicCreated();
    }

    public void checkTopicCreated() {
        Collection<TopicListing> topicList = getTopics();
        int retryCount=1;
        Integer maxAttempts = retryConfigData.getMaxAttempts();
        Long intervalMs = retryConfigData.getInitialIntervalMs();
        Long sleepTimeMs = retryConfigData.getSleepTimeMs();
        Double multiplier = retryConfigData.getMultiplier();

        for(String topicName : kafkaConfigData.getTopicNameToCreate()){
            while(!isTopicCreated(topicList,topicName)){
                checkMaxRetry(retryCount++,maxAttempts);
                sleep(sleepTimeMs);
                sleepTimeMs *= multiplier.intValue();
                topicList = getTopics();
            }
        }
    }

    /**
     * we have to check schema registry up and running because we operate kafka and schema registry
     * in a single docker-compose file. Therefore we want to up kafka and schema registry and running
     * when we run docker-compose command.
     * To check schema registry up and running we should rest call for schema registry server address.
     */
    public void checkSchemaRegistry() {
        // schema registry rest call needs fluent and non-blocking request.
        // So, spring-boot-webflux is the choice for what I want.
        int retryCount=1;
        Integer maxAttempts = retryConfigData.getMaxAttempts();
        Long sleepTimeMs = retryConfigData.getSleepTimeMs();
        Double multiplier = retryConfigData.getMultiplier();

        while(!getSchemaRegistryStatus().is2xxSuccessful()){
            checkMaxRetry(retryCount,maxAttempts);
            sleep(sleepTimeMs);
            sleepTimeMs *= multiplier.intValue(); // exponantial increasing
        }
    }

    private HttpStatus getSchemaRegistryStatus(){
        try {
            return webClient
                    .method(HttpMethod.GET)
                    .uri(kafkaConfigData.getSchemaRegistryUrl())
                    .retrieve()
                    .toBodilessEntity()
                    .map(ResponseEntity::getStatusCode).block();
        } catch (Exception t) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
    }

    private boolean isTopicCreated(Collection<TopicListing> topicListings, String topicName){
        if(topicListings == null)
            return false;
        return topicListings.stream().anyMatch(t->t.name().equalsIgnoreCase(topicName));
    }

    private void checkMaxRetry(int retryCount,Integer maxAttempts){
        if(retryCount > maxAttempts){
            throw new KafkaClientException("Reached max retry limit for reading kafka topic(s)");
        }
    }

    private void sleep(Long milis){
        try{
            Thread.sleep(milis);
        }catch(InterruptedException ex){
            throw new KafkaClientException("Error while sleeping for creating new kafka topic(s)! ");
        }
    }

    private CreateTopicsResult doCreateTopic(RetryContext retryContext) {
        List<String> topicNames = kafkaConfigData.getTopicNameToCreate();
        LOG.info("Create {} topic(s), attempt {} ",topicNames.size(),retryContext.getRetryCount());
        List<NewTopic> topicsList = topicNames.stream()
                .map(topic-> new NewTopic(
                    topic.trim(),
                    kafkaConfigData.getNumOfPartitions(),
                    kafkaConfigData.getReplicationFactor()))
                .collect(Collectors.toList());

        return adminClient.createTopics(topicsList);
    }

    /**
     * topicListing object is another kafka class for getting topics list from cluster.
     */
    private Collection<TopicListing> getTopics(){
        Collection<TopicListing> topicListings;

        try{
            topicListings = retryTemplate.execute(this::doGetTopics);
        }catch(Throwable t){
            throw new KafkaClientException("Reached the max number of retry to reading kafka topic(s).",t);
        }
        return topicListings;
    }

    private Collection<TopicListing> doGetTopics(RetryContext retryContext) throws ExecutionException, InterruptedException {
        LOG.info("Reading kafka topic {}, attempt {}",
                kafkaConfigData.getTopicNameToCreate().toArray(),
                retryContext.getRetryCount());

        // adminClient gets list of topics
       Collection<TopicListing> topics = adminClient.listTopics().listings().get();

       if(topics != null){
           topics.forEach(t-> LOG.debug("Created topic with name {}",t.name()));
       }
       return topics;
    }

}
