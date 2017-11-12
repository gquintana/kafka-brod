package com.github.gquintana.kafka.brod.topic;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class TopicServiceCache implements TopicService {
    private final TopicService delegate;
    private final LoadingCache<String, Optional<Topic>> cache;

    public TopicServiceCache(TopicService delegate, long timeToLive, Executor executor) {
        this.delegate = delegate;
        this.cache = CacheBuilder.newBuilder()
            .expireAfterAccess(timeToLive, TimeUnit.MILLISECONDS)
            .refreshAfterWrite(timeToLive / 2, TimeUnit.MILLISECONDS)
            .build(CacheLoader.asyncReloading(CacheLoader.from(delegate::getTopic), executor));
    }

    @Override
    public void createTopic(Topic topic) {
        delegate.createTopic(topic);
    }

    @Override
    public void deleteTopic(String name) {
        delegate.deleteTopic(name);
        cache.invalidate(name);
    }

    @Override
    public void updateTopic(Topic topic) {
        delegate.updateTopic(topic);
        cache.invalidate(topic.getName());
    }

    @Override
    public boolean existTopic(String name) {
        return delegate.existTopic(name);
    }

    @Override
    public Optional<Topic> getTopic(String name) {
        return cache.getUnchecked(name);
    }

    @Override
    public List<String> getTopics() {
        return delegate.getTopics();
    }
}
