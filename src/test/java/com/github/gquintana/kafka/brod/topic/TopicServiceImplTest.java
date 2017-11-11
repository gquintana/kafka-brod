package com.github.gquintana.kafka.brod.topic;

import com.github.gquintana.kafka.brod.EmbeddedKafkaRule;
import com.github.gquintana.kafka.brod.KafkaService;
import com.github.gquintana.kafka.brod.ZookeeperService;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TopicServiceImplTest {
    @ClassRule
    public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

    @ClassRule
    public static final EmbeddedKafkaRule KAFKA_RULE = new EmbeddedKafkaRule(TEMPORARY_FOLDER, 2);

    private static ZookeeperService zookeeperService;
    private static TopicService topicService;
    private static final Random RANDOM = new Random();

    @BeforeClass
    public static void setUpClass() throws IOException {
        zookeeperService = new ZookeeperService("localhost:2181", 3000, 3000);
        KafkaService kafkaService = new KafkaService("localhost:9092", "partition_service_test");
        topicService = new TopicServiceImpl(zookeeperService, kafkaService);
    }

    @Test
    public void testCreate() throws IOException {
        // Given
        List<String> topics = topicService.getTopics();
        String name = "test_create-" + RANDOM.nextInt(999);
        // When
        topicService.createTopic(new Topic(name, 3, 1, new Properties()));
        // Then
        List<String> topics2 = topicService.getTopics();
        assertThat(topics2.size(), equalTo(topics.size() + 1));
    }

    @Test
    public void testCreateTwice() throws IOException {
        // Given
        List<String> topics = topicService.getTopics();
        String name = "test_create-" + RANDOM.nextInt(999);
        topicService.createTopic(new Topic(name, 3, 1, new Properties()));
        // When
        try {
            topicService.createTopic(new Topic(name, 3, 1, new Properties()));
            // Then
            fail("Exception expected");
        } catch (Exception e) {
        }
    }

    @Test
    public void testDelete() throws IOException {
        // Given
        String name = "test_delete-" + RANDOM.nextInt(999);
        topicService.createTopic(new Topic(name, 3, 1, new Properties()));
        List<String> topics = topicService.getTopics();
        // When
        topicService.deleteTopic(name);
        // Then
        List<String> topics2 = topicService.getTopics();
        Topic topic = topicService.getTopic(name).get();
        assertThat(topics2.size(), equalTo(topics.size()));
    }

    @Test
    public void testDeleteNotFound() throws IOException {
        // Given
        List<String> topics = topicService.getTopics();
        // When
        try {
            topicService.deleteTopic("test_delete-not_found");
        } catch (UnknownTopicOrPartitionException e) {
        }
        // Then
        List<String> topics2 = topicService.getTopics();
        assertThat(topics2.size(), equalTo(topics.size()));
    }

    @Test
    public void testGet() throws IOException {
        // Given
        List<String> topics = topicService.getTopics();
        String name = "test_get-" + RANDOM.nextInt(999);
        topicService.createTopic(new Topic(name, 3, 1, new Properties()));
        KAFKA_RULE.getKafka().send(name, "message");
        // When
        Topic topic = topicService.getTopic(name).get();
        // Then
        assertThat(topic, notNullValue());
        assertThat(topic.getName(), equalTo(name));
        assertThat(topic.getNumPartitions(), equalTo(3));
        assertThat(topic.getReplicationFactor(), equalTo(1));
        assertThat(topic.isInternal(), equalTo(false));
    }

    @Test
    public void testGetInternal() throws IOException {
        // Given
        String name = "test_get_internal-" + RANDOM.nextInt(999);
        topicService.createTopic(new Topic(name, 3, 1, new Properties()));
        KAFKA_RULE.getKafka().send(name, "Message");
        KAFKA_RULE.getKafka().consume(name, "test_get_internal", 5000L);
        // When
        Topic topic = topicService.getTopic("__consumer_offsets").get();
        // Then
        assertThat(topic, notNullValue());
        assertThat(topic.getName(), equalTo("__consumer_offsets"));
        assertThat(topic.isInternal(), equalTo(true));
    }

    @Test
    public void testGetNotFound() throws IOException {
        // Given
        List<String> topics = topicService.getTopics();
        // When
        Topic topic = topicService.getTopic("test_get-not_found").orElse(null);
        // Then
        assertThat(topic, nullValue());
    }

    @Test
    public void testGetPartitions() throws Exception {
        // Given
        String topicName = "test_part_2_2";
        topicService.createTopic(new Topic(topicName, 2, 2, null));
        KAFKA_RULE.getKafka().send(topicName, "Message 1");
        KAFKA_RULE.getKafka().send(topicName, "Message 2");
        KAFKA_RULE.getKafka().send(topicName, "Message 3");
        // When
        List<Partition> partitions = topicService.getTopic(topicName).get().getPartitions();
        // Then
        assertThat(partitions, hasSize(2));
        Partition partition = partitions.get(0);
        assertThat(partition.getReplicas(), hasSize(2));
        assertThat(partition.getReplicas().stream().filter(Replica::isLeader).count(), is(1L));
        assertThat(partition.getReplicas().stream().map(Replica::getBrokerId).collect(Collectors.toSet()), hasSize(2));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        zookeeperService.close();
    }
}
