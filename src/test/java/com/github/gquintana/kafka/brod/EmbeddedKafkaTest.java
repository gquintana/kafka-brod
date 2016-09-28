package com.github.gquintana.kafka.brod;

import org.junit.Ignore;
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
        // When
        EmbeddedZookeeper zookeeper = EmbeddedZookeeper.createAndStart(temporaryFolder);
        // Then
        zookeeper.stop();
    }

    @Test @Ignore
    public void testKafka() throws Exception {
        // Given
        EmbeddedZookeeper zookeeper = EmbeddedZookeeper.createAndStart(temporaryFolder);
        // When
        EmbeddedKafka kafka = EmbeddedKafka.createAndStart(temporaryFolder, 0);
        kafka.send("test_topic", "Hello Kafka");
        List<String> messages = kafka.consume("test_topic", "test_group", 10000L);
        // Then
        assertFalse(messages.isEmpty());
        kafka.stop();
        zookeeper.stop();
    }

    @Test
    public void testMultiKafka() throws Exception {
        // Given
        EmbeddedZookeeper zookeeper = EmbeddedZookeeper.createAndStart(temporaryFolder);
        // When
        EmbeddedKafka kafka0 = EmbeddedKafka.createAndStart(temporaryFolder, 0);
        EmbeddedKafka kafka1 = EmbeddedKafka.createAndStart(temporaryFolder, 1);
        kafka0.send("test_topic", "Hello Kafka");
        List<String> messages = kafka0.consume("test_topic", "test_group", 10000L);
        // Then
        assertFalse(messages.isEmpty());
        kafka0.stop();
        kafka1.stop();
        zookeeper.stop();
    }
}
