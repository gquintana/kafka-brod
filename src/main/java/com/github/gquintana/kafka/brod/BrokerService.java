package com.github.gquintana.kafka.brod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.admin.AdminUtils;
import kafka.server.ConfigType;
import kafka.utils.ZkUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
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
    public Optional<Broker> getBroker(int id) {
        String json = zookeeperService.getData("/brokers/ids/" + id);
        if (json == null) {
            return Optional.empty();
        }
        return Optional.of(parseBroker(id, json));
    }

    Broker parseBroker(int id, String json) {
        try {
            Broker broker = new Broker(id);
            Broker jsonBroker = objectMapper.readValue(json, Broker.class);
            List<Endpoint> endpoints1 = jsonBroker.getEndpoints().stream().map(Endpoint::parse).collect(Collectors.toList());
            // Normalize Port
            if (jsonBroker.getPort() != null && jsonBroker.getPort().intValue() > 0) {
                broker.setPort(jsonBroker.getPort());
            } else if (endpoints1.size() == 1){
                broker.setPort(endpoints1.get(0).port);
            }
            // Normalize JMX Port
            if (jsonBroker.getJmxPort() != null && jsonBroker.getJmxPort().intValue() > 0) {
                broker.setJmxPort(jsonBroker.getJmxPort());
            }
            // Normalize Host
            if (jsonBroker.getHost() != null && !jsonBroker.getHost().isEmpty()) {
                broker.setHost(jsonBroker.getHost());
            } else {
                Optional<String> firstHost = endpoints1.stream().map(e -> e.host).filter(h -> h != null && ! h.isEmpty()).findFirst();
                if (firstHost.isPresent()) {
                    broker.setHost(firstHost.get());
                }
            }
            // Normalize Protocol
            if (jsonBroker.getHost() != null && !jsonBroker.getHost().isEmpty()) {
                broker.setHost(jsonBroker.getHost());
            } else if (endpoints1.size() == 1){
                broker.setProtocol(endpoints1.get(0).protocol);
            }
            broker.setEndpoints(jsonBroker.getEndpoints());
            Optional<Integer> controllerId = getController();
            if (controllerId.isPresent()) {
                broker.setController(id == controllerId.get().intValue());
            }
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

    private static class Controller {
        private int brokerid;
        public int getBrokerid() {
            return brokerid;
        }
        public void setBrokerid(int brokerid) {
            this.brokerid = brokerid;
        }
    }
    /**
     * Get broker elected as controller
     */
    public Optional<Integer> getController() {
        String json = zookeeperService.getData("/controller");
        if (json == null) {
            return Optional.empty();
        }
        try {
            Controller controller = objectMapper.readValue(json, Controller.class);
            return Optional.of(controller.getBrokerid());
        } catch (IOException e) {
            throw new KafkaBrodException("Failed to read or parse controller info", e);
        }
    }
}
