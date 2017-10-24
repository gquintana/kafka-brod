package com.github.gquintana.kafka.brod.broker;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;

@Data
public class Broker {
    private final int id;
    private String host;
    private Integer port;
    private String protocol;
    private Integer jmxPort;
    private List<String> endpoints;
    private Boolean controller;
    private Properties config;
    private Boolean available;
    private SortedMap<String, Object> jmxMetrics;
}
