package com.github.gquintana.kafka.brod;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
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
        EmbeddedKafka kafka = create(temporaryFolder, id);
        kafka.start();
        return kafka;
    }

    static EmbeddedKafka create(TemporaryFolder temporaryFolder, int id) throws IOException {
        File dir = temporaryFolder.newFolder("kafka-" + id);
        return new EmbeddedKafka(id, dir);
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

    public Producer<Long, String> createProducer() {
        Map<String, Object> producerConfig = new HashMap<>();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + port);
        producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer<>(producerConfig);
    }

    public void send(String topic, String message) {
        send(topic, null, message);
    }

    public void send(String topic, Long key, String message) {
        try (Producer<Long, String> producer = createProducer()) {
            ProducerRecord<Long, String> record = new ProducerRecord<>(topic, key, message);
            LOGGER.info("Producing Kafka message");
            try {
                RecordMetadata metadata = producer.send(record).get();
                LOGGER.debug("Produced Kafka message partition {} offset {}", metadata.partition(), metadata.offset());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                LOGGER.error("Producing Kafka message failed", e);
            }
        }
    }

    public org.apache.kafka.clients.consumer.Consumer<Long, String> createConsumer(String groupId) {
        Map<String, Object> consumerConfig = new HashMap<>();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + port);
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        consumerConfig.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        return new KafkaConsumer<>(consumerConfig);
    }
    public List<String> consume(String topic, String groupId, long timeout) {
        try (org.apache.kafka.clients.consumer.Consumer<Long, String> consumer = createConsumer(groupId)) {
            consumer.subscribe(Collections.singletonList(topic));
            return consume(consumer, timeout);
        }
    }

    public List<String> consume(org.apache.kafka.clients.consumer.Consumer<Long, String> consumer, long pollTimeout) {
        LOGGER.info("Consuming Kafka messages");
        List<String> messages = new ArrayList<>();
        long end = System.currentTimeMillis() + pollTimeout;
        while (messages.isEmpty() && System.currentTimeMillis() < end) {
            LOGGER.info("Polling for messages");
            ConsumerRecords<Long, String> records = consumer.poll(pollTimeout / 10L);
            for (ConsumerRecord<Long, String> record : records) {
                LOGGER.info("Found message {} {}", record.key(), record.value());
                messages.add(record.value());
            }
//                consumer.commitSync();
        }
        LOGGER.info("Consumed Kafka {} messages", messages.size());
        String collect = consumer.assignment().stream().map(p -> p.topic() + "-" + p.partition()).collect(Collectors.joining(","));
        LOGGER.info("Assignment {}", collect);
        return messages;
    }

    public void stop() {
        LOGGER.info("Stopping Kafka");
        server.shutdown();
    }

    public void seekToBeggining(String topic, String groupId) {
        Map<String, Object> consumerConfig = new HashMap<>();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + port);
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        consumerConfig.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        try (org.apache.kafka.clients.consumer.Consumer<Long, String> consumer = new KafkaConsumer<>(consumerConfig)) {
            consumer.subscribe(Collections.singletonList(topic));
            Set<TopicPartition> topicPartitions = null;
            while(topicPartitions ==null) {
                topicPartitions = consumer.assignment();
            }
            consumer.seekToBeginning(topicPartitions);
        }
    }
}
