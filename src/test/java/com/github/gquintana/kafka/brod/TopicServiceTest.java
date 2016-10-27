package com.github.gquintana.kafka.brod;

import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TopicServiceTest {
    @ClassRule
    public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

    @ClassRule
    public static final EmbeddedKafkaRule KAFKA_RULE = new EmbeddedKafkaRule(TEMPORARY_FOLDER);

    private static ZookeeperService zookeeperService;
    private static TopicService topicService;
    private static final Random RANDOM = new Random();

    @BeforeClass
    public static void setUpClass() throws IOException {
        zookeeperService = new ZookeeperService("localhost:2181", 3000, 3000);
        topicService = new TopicService(zookeeperService);
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
        // When
        Topic topic = topicService.getTopic(name).get();
        // Then
        assertThat(topic, notNullValue());
        assertThat(topic.getName(), equalTo(name));
        assertThat(topic.getPartitions(), equalTo(3));
        assertThat(topic.getReplicationFactor(), equalTo(0));
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

    @AfterClass
    public static void tearDownClass() throws Exception {
        zookeeperService.close();
    }
}
