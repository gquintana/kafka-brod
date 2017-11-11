package com.github.gquintana.kafka.brod.consumer;

import com.github.gquintana.kafka.brod.cache.Cache;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

public class ConsumerGroupServiceCache implements ConsumerGroupService {
    private final ConsumerGroupService delegate;
    private final Cache<String, ConsumerGroup> groupCache;
    private final Cache<ConsumerKey, Consumer> consumerCache;

    public ConsumerGroupServiceCache(ConsumerGroupService delegate, long timeToLive) {
        this.delegate = delegate;
        groupCache = new Cache<>(delegate::getGroup, timeToLive);
        consumerCache = new Cache<>(this::supplyConsumer, timeToLive);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
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
        return groupCache.get(groupId);
    }

    @Override
    public Optional<ConsumerGroup> getGroup(String groupId, String topic) {
        if (topic == null) {
            return groupCache.get(groupId);
        }
        return delegate.getGroup(groupId, topic);
    }

    private Optional<Consumer> supplyConsumer(ConsumerKey consumerKey) {
        return delegate.getConsumer(consumerKey.groupId, consumerKey.consumerId, null);
    }

    @Override
    public Optional<Consumer> getConsumer(String groupId, String consumerId, String topic) {
        if (topic == null) {
            return consumerCache.get(new ConsumerKey(groupId, consumerId));
        }
        return delegate.getConsumer(groupId, consumerId, topic);
    }
}
