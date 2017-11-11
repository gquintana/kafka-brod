package com.github.gquintana.kafka.brod.consumer;

import com.github.gquintana.kafka.brod.jmx.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Slf4j
public class ConsumerGroupServiceJmx implements ConsumerGroupService {
    private final ConsumerGroupService delegate;
    private final JmxService jmxService;
    private final Map<String, JmxConfiguration> jmxConfigurations;

    public ConsumerGroupServiceJmx(ConsumerGroupService delegate, JmxService jmxService, Map<String, JmxConfiguration> jmxConfigurations) {
        this.delegate = delegate;
        this.jmxService = jmxService;
        this.jmxConfigurations = jmxConfigurations;
    }

    private JmxQuery createJmxQuery(String clientId) {
        return new JmxQuery.Builder()
            .withJavaAttributes()
            .withAttributes("kafka.consumer:type=consumer-coordinator-metrics,client-id=" + clientId, "commit-rate", "heartbeat-rate", "assigned-partitions", "join-rate", "sync-rate")
            .withAttributes("kafka.consumer:type=consumer-fetch-manager-metrics,client-id=" + clientId, "fetch-rate", "records-consumed-rate", "records-lag-max", "bytes-consumed-rate")
            .build();
    }

    private Consumer enrich(String groupId, Consumer consumer) {
        String jmxHost = consumer.getClientHost();
        if (jmxHost == null) {
            jmxHost = consumer.getClientIp();
        }
        JmxConfiguration jmxConfiguration = jmxConfigurations.get(groupId);
        if (jmxHost == null || jmxConfiguration == null || jmxConfiguration.getPort() == null) {
            return consumer;
        }
        try (JmxConnection connection = jmxService.connect(jmxHost, jmxConfiguration.getPort(), jmxConfiguration)) {
            Map<String, Object> jmxMetrics = createJmxQuery(consumer.getClientId()).execute(connection);
            consumer.setJmxMetrics(new TreeMap<>(jmxMetrics));
        } catch (JmxException e) {
            LOGGER.info("Failed to get Consumer {} JMX Metrics: {}, {}", consumer.getId(), e.getMessage(), e.getCause() == null ? "" : e.getCause().getMessage());
        }
        return consumer;
    }

    @Override
    public List<String> getGroupIds() {
        return delegate.getGroupIds();
    }

    @Override
    public Optional<ConsumerGroup> getGroup(String groupId) {
        return delegate.getGroup(groupId);
    }

    @Override
    public Optional<ConsumerGroup> getGroup(String groupId, String topic) {
        return delegate.getGroup(groupId, topic);
    }

    @Override
    public Optional<Consumer> getConsumer(String groupId, String consumerId, String topic) {
        return delegate.getConsumer(groupId, consumerId, topic).map(c -> this.enrich(groupId, c));
    }
}
