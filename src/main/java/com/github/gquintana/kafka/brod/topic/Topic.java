package com.github.gquintana.kafka.brod.topic;

import lombok.Data;

import java.util.Properties;

@Data
public class Topic {
    private final String name;
    private Integer partitions;
    private Integer replicationFactor;
    private boolean internal;
    private Properties config;

    public Topic(String name, Integer partitions, Integer replicationFactor, Properties config) {
        this(name, partitions, replicationFactor, false, config);
    }

    public Topic(String name, Integer partitions, Integer replicationFactor, boolean internal, Properties config) {
        this.name = name;
        this.partitions = partitions;
        this.replicationFactor = replicationFactor;
        this.internal = internal;
        this.config = config == null ? new Properties() : config;
    }
}
