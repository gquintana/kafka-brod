package com.github.gquintana.kafka.brod;

import com.github.gquintana.kafka.brod.broker.BrokersResource;
import com.github.gquintana.kafka.brod.consumer.ConsumerGroupsResource;
import com.github.gquintana.kafka.brod.topic.TopicsResource;
import io.swagger.annotations.Api;

import javax.ws.rs.Path;

@Path("/") @Api
public class KafkaBrodResource {
    private Resources resources;

    public KafkaBrodResource(Resources resources) {
        this.resources = resources;
    }

    @Path("brokers")
    public BrokersResource getBrokers() {
        return resources.brokersResource();
    }

    @Path("topics")
    public TopicsResource getTopics() {
        return resources.topicsResource();
    }

    @Path("/groups")
    public ConsumerGroupsResource getConsumerGroups() {
        return resources.consumerGroupsResource();
    }
}
