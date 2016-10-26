package com.github.gquintana.kafka.brod;

public class Resources {
    private final KafkaBrodApplication application;
    private final BrokersResource brokersResource;
    private final TopicsResource topicsResource;

    public Resources(KafkaBrodApplication application) {
        this.application = application;
        brokersResource = new BrokersResource(this, application.brokerService());
        topicsResource = new TopicsResource(this, application.topicService());
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
}
