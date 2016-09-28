package com.github.gquintana.kafka.brod;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class EmbeddedKafka {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedKafka.class);
    private KafkaServerStartable server;
    private final int id;
    private final int port;
    private final File logDir;

    public EmbeddedKafka(int id, File logDir) {
        this.id = id;
        this.port = 9092 + id;
        this.logDir = logDir;
    }

    public EmbeddedKafka(File logDir) {
        this(0, logDir);
    }

    public static EmbeddedKafka createAndStart(TemporaryFolder temporaryFolder, int id) throws IOException {
        File dir = temporaryFolder.newFolder("kafka-" + id);
        EmbeddedKafka kafka = new EmbeddedKafka(id, dir);
        kafka.start();
        return kafka;
    }

    public void start() throws IOException {
        LOGGER.info("Starting Kafka " + id + " on port " + port);
        Properties properties = TestResources.getResourceAsProperties("/kafka.properties");
        properties.setProperty("broker.id", Integer.toString(id));
        properties.setProperty("log.dirs", logDir.getAbsolutePath());
        properties.setProperty("listeners", "PLAINTEXT://:" + port);
        KafkaConfig config = new KafkaConfig(properties);
        server = new KafkaServerStartable(config);
        server.startup();
    }

    public void send(String topic, String message) {
        Map<String, Object> producerConfig = new HashMap<>();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + port);
        producerConfig.put(ProducerConfig.ACKS_CONFIG, "1");
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        try (Producer<String, String> producer = new KafkaProducer<>(producerConfig)) {
            ProducerRecord producerRecord = new ProducerRecord(topic, message);
            LOGGER.info("Producing Kafka message");
            producer.send(producerRecord);
            producer.flush();
        }
    }

    public List<String> consume(String topic, String groupId, long timeout) {
        Map<String, Object> consumerConfig = new HashMap<>();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + port);
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        try (Consumer<String, String> consumer = new KafkaConsumer<>(consumerConfig)) {
            consumer.subscribe(Arrays.asList(topic));
            LOGGER.info("Consuming Kafka messages");
            List<String> messages = new ArrayList<>();
            long end = System.currentTimeMillis() + timeout;
            LOGGER.debug(consumer.assignment().stream().map(p -> p.topic() + "-" + p.partition()).collect(Collectors.joining(",")));
            while (messages.isEmpty() && System.currentTimeMillis() < end) {
                LOGGER.info("Polling for messages");
                ConsumerRecords<String, String> records = consumer.poll(timeout / 10L);
                for (ConsumerRecord<String, String> record : records) {
                    messages.add(record.value());
                }
                consumer.commitSync();
            }
            return messages;
        }
    }

    public void stop() {
        LOGGER.info("Stopping Kafka");
        server.shutdown();
    }
}
