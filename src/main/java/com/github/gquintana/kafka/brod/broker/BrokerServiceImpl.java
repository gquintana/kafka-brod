package com.github.gquintana.kafka.brod.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gquintana.kafka.brod.KafkaBrodException;
import com.github.gquintana.kafka.brod.KafkaService;
import com.github.gquintana.kafka.brod.ZookeeperService;
import kafka.admin.AdminUtils;
import kafka.server.ConfigType;
import kafka.utils.ZkUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Broker node info service
 */
public class BrokerServiceImpl implements BrokerService {
    private final ZookeeperService zookeeperService;
    private final ObjectMapper objectMapper;
    private final int connectionTimeout;
    private final KafkaService kafkaService;

    public BrokerServiceImpl(ZookeeperService zookeeperService, ObjectMapper objectMapper, int connectionTimeout, KafkaService kafkaService) {
        this.zookeeperService = zookeeperService;
        this.objectMapper = objectMapper;
        this.connectionTimeout = connectionTimeout;
        this.kafkaService = kafkaService;
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
    @Override
    public Optional<Broker> getBroker(int id) {
        String json = zookeeperService.getData("/brokers/ids/" + id);
        if (json == null) {
            return Optional.empty();
        }
        Broker broker = parseBroker(id, json);
        Properties properties = AdminUtils.fetchEntityConfig(getZkUtils(), ConfigType.Broker(), Integer.toString(id));
        broker.setConfig(properties);
        return Optional.of(broker);
    }

    Broker parseBroker(int id, String json) {
        try {
            Broker broker = new Broker(id);
            Broker jsonBroker = objectMapper.readValue(json, Broker.class);
            List<Endpoint> endpoints1 = jsonBroker.getEndpoints().stream().map(Endpoint::parse).collect(Collectors.toList());
            // Normalize Port
            if (jsonBroker.getPort() != null && jsonBroker.getPort() > 0) {
                broker.setPort(jsonBroker.getPort());
            } else if (endpoints1.size() == 1){
                broker.setPort(endpoints1.get(0).port);
            }
            // Normalize JMX Port
            if (jsonBroker.getJmxPort() != null && jsonBroker.getJmxPort() > 0) {
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
            if (jsonBroker.getProtocol() != null && !jsonBroker.getProtocol().isEmpty()) {
                broker.setProtocol(jsonBroker.getProtocol());
            } else if (endpoints1.size() == 1){
                broker.setProtocol(endpoints1.get(0).protocol);
            }
            broker.setEndpoints(jsonBroker.getEndpoints());
            Optional<Integer> controllerId = getControllerId();
            if (controllerId.isPresent()) {
                broker.setController(id == controllerId.get());
            }
            if (broker.getHost() != null && broker.getPort() != null) {
                boolean ssl = broker.getProtocol() != null && broker.getProtocol().contains("SSL");
                broker.setAvailable(isAvailable(broker.getHost(), broker.getPort(), ssl, connectionTimeout));
            }
            return broker;
        } catch (IOException e) {
            throw new KafkaBrodException("Failed to read or parse broker info", e);
        }
    }

    /**
     * List broker Ids
     */
    @Override
    public List<Broker> getBrokers() {
        DescribeClusterResult cluster = kafkaService.adminClient().describeCluster();
        KafkaFuture<Node> controllerFuture = cluster.controller();
        KafkaFuture<Collection<Node>> nodesFuture = cluster.nodes();
        try {
            Node controller = controllerFuture.get();
            Collection<Node> nodes = nodesFuture.get();
            return nodes.stream().map(n -> this.convertToBroker(n, controller)).collect(Collectors.toList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        } catch (ExecutionException e) {
            throw new KafkaBrodException("Failed to get brokers", e);
        }
    }

    private Broker convertToBroker(Node node, Node controller) {
        Broker broker = new Broker(node.id());
        broker.setController(controller != null && node.id() == controller.id());
        broker.setHost(node.host());
        broker.setPort(node.port());
        return broker;
    }

    @Getter @Setter
    private static class Controller {
        private int brokerid;
    }
    /**
     * Get broker elected as controller
     */
    private Optional<Integer> getControllerId() {
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

    @Override
    public Optional<Broker> getControllerBroker() {
        return getControllerId().flatMap(this::getBroker);
    }

    /**
     * Try to open a socket on broker to tes whether it is running
     */
    private static boolean isAvailable(String host, int port, boolean ssl, int connectTimeout) {
        SocketFactory socketFactory = ssl ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
        try(Socket socket = socketFactory.createSocket()) {
            socket.setSoTimeout(connectTimeout);
            socket.connect(new InetSocketAddress(host, port), connectTimeout);
            return socket.isConnected();
        } catch (IOException e) {
            return false;
        }
    }
}
