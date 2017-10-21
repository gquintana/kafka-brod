package com.github.gquintana.kafka.brod.broker;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;

public class Broker {
    private int id;
    private String host;
    private Integer port;
    private String protocol;
    private Integer jmxPort;
    private List<String> endpoints;
    private Boolean controller;
    private Properties config;
    private Boolean available;
    private SortedMap<String, Object> jmxMetrics;

    public Broker() {
    }

    public Broker(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getJmxPort() {
        return jmxPort;
    }

    public void setJmxPort(Integer jmxPort) {
        this.jmxPort = jmxPort;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }

    public Boolean getController() {
        return controller;
    }

    public void setController(Boolean controller) {
        this.controller = controller;
    }

    public Properties getConfig() {
        return config;
    }

    public void setConfig(Properties config) {
        this.config = config;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Map<String, Object> getJmxMetrics() {
        return jmxMetrics;
    }

    public void setJmxMetrics(SortedMap<String, Object> jmxMetrics) {
        this.jmxMetrics = jmxMetrics;
    }
}
