package com.github.gquintana.kafka.brod.broker;

import com.github.gquintana.kafka.brod.jmx.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
@Slf4j
public class BrokerServiceJmx implements BrokerService {
    private final BrokerService delegate;
    private final JmxService jmxService;
    private final JmxConfiguration jmxConfiguration;
    private final JmxQuery jmxQuery = new JmxQuery.Builder()
        .withJavaAttributes()
        .withJavaCmsGcAttributes()
        .withRateAttributes("kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec")
        .withRateAttributes("kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec")
        .withRateAttributes("kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec")
        .build();

    public BrokerServiceJmx(BrokerService delegate, JmxService jmxService, JmxConfiguration jmxConfiguration) {
        this.delegate = delegate;
        this.jmxService = jmxService;
        this.jmxConfiguration = jmxConfiguration;
    }

    private Broker enrich(Broker broker) {
        if (broker.getHost() == null || broker.getJmxPort() == null) {
            return broker;
        }
        try (JmxConnection connection = jmxService.connect(broker.getHost(), broker.getJmxPort(), jmxConfiguration)) {
            broker.setJmxMetrics(new TreeMap<>(jmxQuery.execute(connection)));
        } catch (JmxException e) {
            LOGGER.info("Failed to get Broker {} JMX Metrics: {}, {}", broker.getId(), e.getMessage(), e.getCause() == null ? "" : e.getCause().getMessage());
        }
        return broker;
    }

    @Override
    public Optional<Broker> getBroker(int id) {
        return delegate.getBroker(id).map(this::enrich);
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
