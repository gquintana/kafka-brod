package com.github.gquintana.kafka.brod.broker;

import com.github.gquintana.kafka.brod.jmx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

public class BrokerJmxService {
    private final JmxService jmxService;
    private final JmxConfiguration jmxConfiguration;
    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerJmxService.class);
    private final JmxQuery jmxQuery = new JmxQuery.Builder()
        .withJavaAttributes()
        .withJavaCmsGcAttributes()
        .withRateAttributes("kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec")
        .withRateAttributes("kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec")
        .withRateAttributes("kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec")
        .build();

    public BrokerJmxService(JmxService jmxService, JmxConfiguration jmxConfiguration) {
        this.jmxService = jmxService;
        this.jmxConfiguration = jmxConfiguration;
    }

    public Broker enrich(Broker broker) {
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
}
