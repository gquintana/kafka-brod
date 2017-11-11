package com.github.gquintana.kafka.brod.topic;

import lombok.Data;

import java.util.List;
import java.util.Properties;

@Data
public class Topic {
    private final String name;
    private final Integer numPartitions;
    private final Integer replicationFactor;
    private final boolean internal;
    private final Properties config;
    private final List<Partition> partitions;

    public Topic(String name, Integer numPartitions, Integer replicationFactor, Properties config) {
        this(name, numPartitions, replicationFactor, false, config, null);
    }

    public Topic(String name, Integer numPartitions, Integer replicationFactor, boolean internal, Properties config, List<Partition> partitions) {
        this.name = name;
        this.numPartitions = numPartitions;
        this.replicationFactor = replicationFactor;
        this.internal = internal;
        this.config = config == null ? new Properties() : config;
        this.partitions = partitions;
    }
}
