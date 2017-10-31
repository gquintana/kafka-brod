package com.github.gquintana.kafka.brod.topic;

import com.github.gquintana.kafka.brod.broker.Broker;
import com.github.gquintana.kafka.brod.jmx.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class PartitionJmxService {
    private final JmxService jmxService;
    private final Supplier<Broker> brokerSupplier;
    private final JmxConfiguration jmxConfiguration;

    public PartitionJmxService(JmxService jmxService, Supplier<Broker> brokerSupplier, JmxConfiguration jmxConfiguration) {
        this.jmxService = jmxService;
        this.brokerSupplier = brokerSupplier;
        this.jmxConfiguration = jmxConfiguration;
    }

    public Partition enrich(Partition partition) {
        List<Partition> partitions = Collections.singletonList(partition);
        enrich(partitions);
        return partitions.get(0);
    }

    public List<Partition> enrich(List<Partition> partitions) {
        Broker broker = brokerSupplier.get();
        if (partitions == null | partitions.isEmpty()
            || broker == null || broker.getHost() == null || broker.getJmxPort() == null) {
            return partitions;
        }
        try (JmxConnection connection = jmxService.connect(broker.getHost(), broker.getJmxPort(), jmxConfiguration)) {
            for (Partition partition : partitions) {
                String mBeanPrefix = String.format("kafka.log:type=Log,topic=%s,partition=%d", partition.getTopicName(), partition.getId());
                Map<String, Object> metrics = new JmxQuery.Builder()
                    .withAttributes(mBeanPrefix + ",name=Size", "Value")
                    .withAttributes(mBeanPrefix + ",name=NumLogSegments", "Value")
                    .build().execute(connection);
                partition.setSize(toLong(metrics.get("kafka_log.log.size.value")));
                partition.setNumSegments(toLong(metrics.get("kafka_log.log.num_log_segments.value")));
            }
        } catch (JmxException e) {
            LOGGER.info("Failed to get Topic {} JMX Metrics: {}, {}", broker.getId(), e.getMessage(), e.getCause() == null ? "" : e.getCause().getMessage());
        }
        return partitions;
    }

    private static Long toLong(Object o) {
        return o instanceof Number ? ((Number) o).longValue() : null;
    }
}
