package com.github.gquintana.kafka.brod;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
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

    @Test
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
    public void testKafka_Seek() throws Exception {
        // Given
        EmbeddedZookeeper zookeeper = EmbeddedZookeeper.createAndStart(temporaryFolder);
        // When
        EmbeddedKafka kafka = EmbeddedKafka.createAndStart(temporaryFolder, 0);
        try(Producer<Long, String> producer = kafka.createProducer()) {
            for (long i = 0; i < 100; i++) {
                producer.send(new ProducerRecord<>("test_topic_seek", i, "Hello Kafka " + i)).get();
            }
        }
        try(Consumer<Long, String> consumer = kafka.createConsumer("test_group")) {
            consumer.subscribe(Arrays.asList("test_topic_seek"));
            List<String> messages = kafka.consume(consumer, 1000L);
            assertFalse(messages.isEmpty());
            consumer.seekToBeginning(consumer.assignment());
            kafka.seekToBeggining("test_topic_seek", "test_group");
            List<String> messages2 = kafka.consume(consumer, 1000L);
            // Then
            assertFalse(messages2.isEmpty());
            assertEquals(messages.get(0), messages2.get(0));
        }
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
