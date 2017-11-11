package com.github.gquintana.kafka.brod.topic;

import com.github.gquintana.kafka.brod.cache.Cache;

import java.util.List;
import java.util.Optional;

public class TopicServiceCache implements TopicService {
    private final TopicService delegate;
    private final Cache<String, Topic> cache;

    public TopicServiceCache(TopicService delegate, long timeToLive) {
        this.delegate = delegate;
        this.cache = new Cache<>(delegate::getTopic, timeToLive);
    }

    @Override
    public void createTopic(Topic topic) {
        delegate.createTopic(topic);
    }

    @Override
    public void deleteTopic(String name) {
        delegate.deleteTopic(name);
        cache.remove(name);
    }

    @Override
    public void updateTopic(Topic topic) {
        delegate.updateTopic(topic);
        cache.remove(topic.getName());
    }

    @Override
    public boolean existTopic(String name) {
        return delegate.existTopic(name);
    }

    @Override
    public Optional<Topic> getTopic(String name) {
        return cache.get(name);
    }

    @Override
    public List<String> getTopics() {
        return delegate.getTopics();
    }
}
