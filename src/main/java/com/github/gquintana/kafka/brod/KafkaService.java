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
    private final Map<String, Object> clientConfig;
    private final ObjectExpirer<AdminClient> adminClient;
    private final ObjectExpirer<kafka.admin.AdminClient> scalaAdminClient;

    public KafkaService(String bootstrapServers, String clientId) {
        this(bootstrapServers, new HashMap<>());
    }

    public KafkaService(String bootstrapServers, Map<String, Object> clientConfig) {
        this.bootstrapServers = bootstrapServers;
        String lClientId = (String) clientConfig.get(CommonClientConfigs.CLIENT_ID_CONFIG);
        if (lClientId == null) {
            clientConfig.put(CommonClientConfigs.CLIENT_ID_CONFIG, generateClientId());
        }
        long lMaxIdleMs = Long.parseLong(clientConfig.getOrDefault(CommonClientConfigs.CONNECTIONS_MAX_IDLE_MS_CONFIG, 540000).toString());
        clientConfig.put(CommonClientConfigs.CONNECTIONS_MAX_IDLE_MS_CONFIG, lMaxIdleMs);
        this.clientConfig = clientConfig;
        adminClient = new ObjectExpirer<>(this::createAdminClient, lMaxIdleMs);
        scalaAdminClient = new ObjectExpirer<>(this::createScalaAdminClient, lMaxIdleMs);
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

    private Map<String, Object> createClientConfig() {
        Map<String, Object> config = new HashMap<>();
        config.putAll(clientConfig);
        config.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return config;
    }

    private AdminClient createAdminClient() {
        LOGGER.info("Connecting to Kafka Client Admin");
        Map<String, Object> config = createClientConfig();
        return AdminClient.create(config);
    }

    private kafka.admin.AdminClient createScalaAdminClient() {
        LOGGER.info("Connecting to Kafka Internal Admin");
        Map<String, Object> config = createClientConfig();
        return kafka.admin.AdminClient.create(JavaConversions.mapAsScalaMap(config).toMap(Predef.conforms()));
    }

    public AdminClient adminClient() {
        return adminClient.get();
    }

    public kafka.admin.AdminClient scalaAdminClient() {
        return scalaAdminClient.get();
    }

    public Consumer<String, String> consumer(String groupId) {
        Map<String, Object> consumerConfig = createClientConfig();
        consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
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
