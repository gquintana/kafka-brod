package com.github.gquintana.kafka.brod;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import scala.Predef;
import scala.collection.JavaConversions;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class KafkaService implements AutoCloseable {
    private final String bootstrapServers;
    private final String clientId;
    private AdminClient adminClient;
    private kafka.admin.AdminClient scalaAdminClient;

    public KafkaService(String bootstrapServers, String clientId) {
        this.bootstrapServers = bootstrapServers;
        if (clientId == null) {
            clientId = generateClientId();
        }
        this.clientId = clientId;
    }

    private static String generateClientId() {
        String clientId;
        clientId = "kafka-brod";
        try {
            clientId = clientId + "-" + InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        }
        return clientId;
    }

    public synchronized AdminClient adminClient() {
        if (adminClient == null) {
            Map<String, Object> clientConfig = new HashMap<>();
            clientConfig.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            clientConfig.put(CommonClientConfigs.CLIENT_ID_CONFIG, clientId);
            adminClient = AdminClient.create(clientConfig);
        }
        return adminClient;
    }

    public synchronized kafka.admin.AdminClient scalaAdminClient() {
        if (scalaAdminClient == null) {
            Map<String, Object> brokerConfig = new HashMap<>();
            brokerConfig.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            brokerConfig.put(CommonClientConfigs.CLIENT_ID_CONFIG, clientId);
            scalaAdminClient = kafka.admin.AdminClient.create(JavaConversions.mapAsScalaMap(brokerConfig).toMap(Predef.conforms()));
        }
        return scalaAdminClient;
    }

    public Consumer<String, String> consumer(String groupId) {
        Map<String, Object> consumerConfig = new HashMap<>();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerConfig.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId + "-" + Thread.currentThread().getName());
        consumerConfig.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return new KafkaConsumer<>(consumerConfig);
    }

    @Override
    public synchronized void close() {
        if (adminClient != null) {
            adminClient.close();
            adminClient = null;
        }
        if (scalaAdminClient != null) {
            scalaAdminClient.close();
            scalaAdminClient = null;
        }
    }
}
