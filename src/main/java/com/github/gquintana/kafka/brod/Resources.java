package com.github.gquintana.kafka.brod;

import com.github.gquintana.kafka.brod.broker.BrokersResource;
import com.github.gquintana.kafka.brod.consumer.ConsumerGroupResource;
import com.github.gquintana.kafka.brod.consumer.ConsumerGroupsResource;
import com.github.gquintana.kafka.brod.topic.PartitionsResource;
import com.github.gquintana.kafka.brod.topic.TopicResource;
import com.github.gquintana.kafka.brod.topic.TopicsResource;

public class Resources {
    private final KafkaBrodApplication application;
    private final KafkaBrodResource applicationResource;
    private final BrokersResource brokersResource;
    private final TopicsResource topicsResource;
    private final ConsumerGroupsResource consumerGroupsResource;
    private final StaticResource uiResource;
    private final ApiResource apiResource;

    public Resources(KafkaBrodApplication application) {
        this.application = application;
        this.applicationResource = new KafkaBrodResource(this);
        brokersResource = new BrokersResource(this, application.brokerService());
        topicsResource = new TopicsResource(this, application.topicService());
        consumerGroupsResource = new ConsumerGroupsResource(this, application.consumerGroupService());
        apiResource = new ApiResource(this);
        uiResource = new StaticResource();

    }

    public BrokersResource brokersResource() {
        return brokersResource;
    }

    public TopicsResource topicsResource() {
        return topicsResource;
    }

    public TopicResource topicResource(String name) {
        return new TopicResource(this, application.topicService(), name);
    }

    public PartitionsResource partitionsResource(String topic) {
        return new PartitionsResource(this, application.partitionService(), topic);
    }

    public ConsumerGroupsResource consumerGroupsResource() {
        return consumerGroupsResource;
    }

    public ConsumerGroupResource consumerGroupResource(String groupId) {
        return new ConsumerGroupResource(this, application.consumerGroupService(), groupId);
    }

    public KafkaBrodResource applicationResource() {
        return this.applicationResource;
    }

    public StaticResource uiResource() {
        return uiResource;
    }

    public ApiResource apiResource() {
        return apiResource;
    }
}
