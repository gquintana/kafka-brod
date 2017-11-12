package com.github.gquintana.kafka.brod.consumer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class ConsumerGroupServiceCache implements ConsumerGroupService {
    private final ConsumerGroupService delegate;
    private final LoadingCache<String, Optional<ConsumerGroup>> groupCache;
    private final LoadingCache<ConsumerKey, Optional<Consumer>> consumerCache;

    public ConsumerGroupServiceCache(ConsumerGroupService delegate, long timeToLive, Executor executor) {
        this.delegate = delegate;
        this.groupCache= CacheBuilder.newBuilder()
            .expireAfterAccess(timeToLive, TimeUnit.MILLISECONDS)
            .refreshAfterWrite(timeToLive / 2, TimeUnit.MILLISECONDS)
            .build(CacheLoader.asyncReloading(CacheLoader.from(delegate::getGroup), executor));
        this.consumerCache= CacheBuilder.newBuilder()
            .expireAfterAccess(timeToLive, TimeUnit.MILLISECONDS)
            .refreshAfterWrite(timeToLive / 2, TimeUnit.MILLISECONDS)
            .build(CacheLoader.asyncReloading(CacheLoader.from(this::loadConsumer), executor));
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode
    private static final class ConsumerKey {
        private final String groupId;
        private final String consumerId;
    }

    @Override
    public List<String> getGroupIds() {
        return delegate.getGroupIds();
    }

    @Override
    public Optional<ConsumerGroup> getGroup(String groupId) {
        return groupCache.getUnchecked(groupId);
    }

    @Override
    public Optional<ConsumerGroup> getGroup(String groupId, String topic) {
        if (topic == null) {
            return groupCache.getUnchecked(groupId);
        }
        return delegate.getGroup(groupId, topic);
    }

    private Optional<Consumer> loadConsumer(ConsumerKey consumerKey) {
        return delegate.getConsumer(consumerKey.groupId, consumerKey.consumerId, null);
    }

    @Override
    public Optional<Consumer> getConsumer(String groupId, String consumerId, String topic) {
        if (topic == null) {
            return consumerCache.getUnchecked(new ConsumerKey(groupId, consumerId));
        }
        return delegate.getConsumer(groupId, consumerId, topic);
    }
}
