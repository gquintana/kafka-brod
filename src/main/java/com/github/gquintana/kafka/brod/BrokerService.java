package com.github.gquintana.kafka.brod;

import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.utils.ZkUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Broker node info service
 */
public class BrokerService {
    private final ZookeeperService zookeeperService;
    private final ObjectMapper objectMapper;

    public BrokerService(ZookeeperService zookeeperService, ObjectMapper objectMapper) {
        this.zookeeperService = zookeeperService;
        this.objectMapper = objectMapper;
    }

    private ZkUtils getZkUtils() {
        return zookeeperService.getZkUtils();
    }

    private static final Pattern ENDPOINT_REGEXP = Pattern.compile("^(\\w+)://(.*):(\\d+)$");

    private static class Endpoint {
        final String protocol;
        final String host;
        final int port;

        private Endpoint(String protocol, String host, int port) {
            this.protocol = protocol;
            this.host = host;
            this.port = port;
        }

        private static Endpoint parse(String endpoint) {
            Matcher matcher = ENDPOINT_REGEXP.matcher(endpoint);
            if (!matcher.matches()) {
                return null;
            }
            String protocol = matcher.group(1);
            String host = matcher.group(2);
            int port = Integer.parseInt(matcher.group(3));
            return new Endpoint(protocol, host, port);
        }
    }

    /**
     * Get broker detailed info
     */
    public Broker getBroker(int id) {
        String json = zookeeperService.getData("/brokers/ids/" + id);
        if (json == null) {
            return null;
        }
        return parseBroker(id, json);
    }

    Broker parseBroker(int id, String json) {
        try {
            Broker broker = new Broker(id);
            Broker jsonBroker = objectMapper.readValue(json, Broker.class);
            List<Endpoint> endpoints1 = jsonBroker.getEndpoints().stream().map(Endpoint::parse).collect(Collectors.toList());
            if (jsonBroker.getPort() != null && jsonBroker.getPort().intValue() > 0) {
                broker.setPort(jsonBroker.getPort());
            } else if (endpoints1.size() == 1){
                broker.setPort(endpoints1.get(0).port);
            }
            if (jsonBroker.getJmxPort() != null && jsonBroker.getJmxPort().intValue() > 0) {
                broker.setJmxPort(jsonBroker.getJmxPort());
            }
            if (jsonBroker.getHost() != null) {
                broker.setHost(jsonBroker.getHost());
            } else {
                Optional<String> firstHost = endpoints1.stream().map(e -> e.host).filter(h -> h != null && ! h.isEmpty()).findFirst();
                if (firstHost.isPresent()) {
                    broker.setHost(firstHost.get());
                }
            }
            broker.setEndpoints(jsonBroker.getEndpoints());
            return broker;
        } catch (IOException e) {
            throw new KafkaBrodException("Failed to read or parse broker info", e);
        }
    }

    /**
     * List broker Ids
     */
    public List<Integer> getBrokers() {
        return zookeeperService.getChildren("/brokers/ids").stream()
                .map(Integer::valueOf)
                .sorted()
                .collect(Collectors.toList());
    }
}
