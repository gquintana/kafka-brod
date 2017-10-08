package com.github.gquintana.kafka.brod;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;

import java.util.HashMap;
import java.util.Map;

public class KafkaService implements AutoCloseable {
    private AdminClient adminClient;
    private Map<String, Object> clientConfig;

    public KafkaService(String bootstrapServers, String clientId) {
        clientConfig = new HashMap<>();
        clientConfig.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        clientConfig.put(CommonClientConfigs.CLIENT_ID_CONFIG, clientId == null ? "kafka-brod" : clientId);
    }

    public synchronized AdminClient adminClient() {
        if (adminClient == null) {
            adminClient = AdminClient.create(clientConfig);
        }
        return adminClient;
    }

    @Override
    public synchronized void close() {
        if (adminClient != null) {
            adminClient.close();
            adminClient = null;
        }
    }
}
