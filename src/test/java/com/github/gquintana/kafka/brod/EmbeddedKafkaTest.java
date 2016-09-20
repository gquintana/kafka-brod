package com.github.gquintana.kafka.brod;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertFalse;

public class EmbeddedKafkaTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testZookeeper() throws IOException {
        // Given
        File zookeeperData = temporaryFolder.newFolder("zookeeper");
        EmbeddedZookeeper zookeeper = new EmbeddedZookeeper(zookeeperData);
        // When
        zookeeper.start();
        // Then
        zookeeper.stop();
    }

    @Test
    public void testKafka() throws IOException {
        // Given
        File zookeeperData = temporaryFolder.newFolder("zookeeper");
        EmbeddedZookeeper zookeeper = new EmbeddedZookeeper(zookeeperData);
        File kafkaData = temporaryFolder.newFolder("kafka");
        EmbeddedKafka kafka = new EmbeddedKafka(kafkaData);
        // When
        zookeeper.start();
        kafka.start();
        kafka.send("test_topic", "Hello Kafka");
        List<String> messages = kafka.consume("test_topic", "test_group", 10000L);
        // Then
        assertFalse(messages.isEmpty());
        kafka.stop();
        zookeeper.stop();
    }
}
