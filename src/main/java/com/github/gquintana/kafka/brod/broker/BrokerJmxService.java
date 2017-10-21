package com.github.gquintana.kafka.brod.broker;

import com.github.gquintana.kafka.brod.jmx.JmxConnection;
import com.github.gquintana.kafka.brod.jmx.JmxException;
import com.github.gquintana.kafka.brod.jmx.JmxQuery;
import com.github.gquintana.kafka.brod.jmx.JmxService;

import java.util.TreeMap;

public class BrokerJmxService {
    private final JmxService jmxService;
    private final JmxQuery jmxQuery = new JmxQuery.Builder()
        .withJavaAttributes()
        .withJavaCmsGcAttributes()
        .withRateAttributes("kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec")
        .withRateAttributes("kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec")
        .withRateAttributes("kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec")
        .build();

    public BrokerJmxService(JmxService jmxService) {
        this.jmxService = jmxService;
    }

    public Broker enrich(Broker broker) {
        if (broker.getHost() == null || broker.getJmxPort() == null) {
            return broker;
        }
        try (JmxConnection connection = jmxService.connect(broker.getHost(), broker.getJmxPort())) {
            broker.setJmxMetrics(new TreeMap<>(jmxQuery.execute(connection)));
        } catch (JmxException e) {
            // TODO
        }
        return broker;
    }
}
