package com.github.gquintana.kafka.brod.consumer;

import lombok.Data;

import java.util.List;
import java.util.SortedMap;

@Data
public class Consumer {
    private String clientId;
    private String clientHost;
    private String clientIp;
    private String id;
    private List<ConsumerPartition> partitions;
    private SortedMap<String, Object> jmxMetrics;
}
