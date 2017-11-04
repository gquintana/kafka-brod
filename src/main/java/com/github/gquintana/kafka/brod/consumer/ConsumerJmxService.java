package com.github.gquintana.kafka.brod.consumer;

import com.github.gquintana.kafka.brod.jmx.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class ConsumerJmxService {
    private final JmxService jmxService;
    private final Map<String, JmxConfiguration> jmxConfigurations;

    public ConsumerJmxService(JmxService jmxService, Map<String, JmxConfiguration> jmxConfigurations) {
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

    public Consumer enrich(String groupId, Consumer consumer) {
        String jmxHost = consumer.getClientHost();
        JmxConfiguration jmxConfiguration = jmxConfigurations.get(groupId);
        if (jmxHost == null || jmxConfiguration == null || jmxConfiguration.getPort() == null) {
            return consumer;
        }
        if (jmxHost.startsWith("/")) {
            jmxHost = jmxHost.substring(1);
        }
        try (JmxConnection connection = jmxService.connect(jmxHost, jmxConfiguration.getPort(), jmxConfiguration)) {
            Map<String, Object> jmxMetrics = createJmxQuery(consumer.getClientId()).execute(connection);
            consumer.setJmxMetrics(new TreeMap<>(jmxMetrics));
        } catch (JmxException e) {
            LOGGER.info("Failed to get Consumer {} JMX Metrics: {}, {}", consumer.getId(), e.getMessage(), e.getCause() == null ? "" : e.getCause().getMessage());
        }
        return consumer;
    }
}
