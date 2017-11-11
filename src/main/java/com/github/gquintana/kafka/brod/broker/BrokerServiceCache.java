package com.github.gquintana.kafka.brod.broker;

import com.github.gquintana.kafka.brod.cache.Cache;

import java.util.List;
import java.util.Optional;

public class BrokerServiceCache implements BrokerService {
    private final BrokerService delegate;
    private final Cache<Integer, Broker> cache;

    public BrokerServiceCache(BrokerService delegate, long timeToLive) {
        this.delegate = delegate;
        this.cache = new Cache<>(delegate::getBroker, timeToLive);
    }

    @Override
    public Optional<Broker> getBroker(int id) {
        return cache.get(id);
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
