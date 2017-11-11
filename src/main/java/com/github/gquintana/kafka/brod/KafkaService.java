package com.github.gquintana.kafka.brod;

import com.github.gquintana.kafka.brod.util.ObjectExpirer;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class KafkaService implements AutoCloseable {
    private final String bootstrapServers;
    private final String clientId;
    private final long maxIdleMs;
    private final ObjectExpirer<AdminClient> adminClient;
    private final ObjectExpirer<kafka.admin.AdminClient> scalaAdminClient;

    public KafkaService(String bootstrapServers, String clientId) {
        this(bootstrapServers, clientId, 540000);
    }

    public KafkaService(String bootstrapServers, String clientId, long maxIdleMs) {
        this.bootstrapServers = bootstrapServers;
        this.maxIdleMs = maxIdleMs;
        if (clientId == null) {
            clientId = generateClientId();
        }
        this.clientId = clientId;
        adminClient = new ObjectExpirer<>(this::createAdminClient, maxIdleMs);
        scalaAdminClient = new ObjectExpirer<>(this::createScalaAdminClient, maxIdleMs);
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

    private AdminClient createAdminClient() {
        LOGGER.info("Connecting to Kafka Client Admin");
        Map<String, Object> config = new HashMap<>();
        config.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(CommonClientConfigs.CLIENT_ID_CONFIG, clientId);
        config.put(CommonClientConfigs.CONNECTIONS_MAX_IDLE_MS_CONFIG, maxIdleMs);
        return AdminClient.create(config);
    }

    private kafka.admin.AdminClient createScalaAdminClient() {
        LOGGER.info("Connecting to Kafka Internal Admin");
        Map<String, Object> config = new HashMap<>();
        config.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(CommonClientConfigs.CLIENT_ID_CONFIG, clientId);
        config.put(CommonClientConfigs.CONNECTIONS_MAX_IDLE_MS_CONFIG, maxIdleMs);
        return kafka.admin.AdminClient.create(JavaConversions.mapAsScalaMap(config).toMap(Predef.conforms()));
    }

    public AdminClient adminClient() {
        return adminClient.get();
    }

    public kafka.admin.AdminClient scalaAdminClient() {
        return scalaAdminClient.get();
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
        adminClient.close();
        scalaAdminClient.close();
    }
}
