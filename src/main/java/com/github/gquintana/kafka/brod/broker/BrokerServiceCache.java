package com.github.gquintana.kafka.brod.broker;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class BrokerServiceCache implements BrokerService {
    private final BrokerService delegate;
    private final LoadingCache<Integer, Optional<Broker>> cache;

    public BrokerServiceCache(BrokerService delegate, long timeToLive, Executor executor) {
        this.delegate = delegate;
        this.cache = CacheBuilder.newBuilder()
            .expireAfterAccess(timeToLive, TimeUnit.MILLISECONDS)
            .refreshAfterWrite(timeToLive / 2, TimeUnit.MILLISECONDS)
            .build(CacheLoader.asyncReloading(CacheLoader.from(delegate::getBroker), executor));
    }

    @Override
    public Optional<Broker> getBroker(int id) {
        return cache.getUnchecked(id);
    }

    @Override
    public List<Broker> getBrokers() {
        return delegate.getBrokers();
    }

    @Override
    public Optional<Broker> getControllerBroker() {
        return delegate.getControllerBroker();
    }
}
