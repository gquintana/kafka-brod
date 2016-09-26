package com.github.gquintana.kafka.brod;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

public class KafkaBrodApplication {
    private ObjectMapper objectMapper;
    private ZookeeperService zookeeperService;
    private BrokerService brokerService;
    private TopicService topicService;

    public void run() throws Exception {
        zookeeperService = new ZookeeperService("localhost:2181", 3000, 3000);
        zookeeperService.connect();

        objectMapper =  new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        brokerService = new BrokerService(zookeeperService, objectMapper);

        topicService = new TopicService(zookeeperService);
    }
}
