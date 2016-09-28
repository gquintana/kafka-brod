package com.github.gquintana.kafka.brod;

import java.util.Properties;

public class Topic {
    private String name;
    private Integer partitions;
    private Integer replicationFactor;
    private Properties config;

    public Topic() {
    }

    public Topic(String name, Integer partitions, Integer replicationFactor, Properties config) {
        this.name = name;
        this.partitions = partitions;
        this.replicationFactor = replicationFactor;
        this.config = config == null ? new Properties() : config;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPartitions() {
        return partitions;
    }

    public void setPartitions(Integer partitions) {
        this.partitions = partitions;
    }

    public Integer getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(Integer replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    public Properties getConfig() {
        return config;
    }

    public void setConfig(Properties config) {
        this.config = config;
    }
}
