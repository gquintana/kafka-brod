package com.github.gquintana.kafka.brod.topic;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.github.gquintana.kafka.brod.EmbeddedKafkaRule;
import com.github.gquintana.kafka.brod.ZookeeperService;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PartitionServiceTest {
    @ClassRule
    public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

    @ClassRule
    public static final EmbeddedKafkaRule KAFKA_RULE = new EmbeddedKafkaRule(TEMPORARY_FOLDER, 3);

    private static PartitionService partitionService;
    private static TopicService topicService;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ZookeeperService zookeeperService = new ZookeeperService("localhost:2181", 3000, 3000);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        topicService = new TopicService(zookeeperService);
        partitionService = new PartitionService(zookeeperService);
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
        List<Partition> partitions = partitionService.getPartitions(topicName);
        // Then
        assertThat(partitions, hasSize(2));
        Partition partition = partitions.get(0);
        assertThat(partition.getReplicas(), hasSize(2));
        assertThat(partition.getReplicas().stream().filter(Replica::isLeader).count(), is(1L));
        assertThat(partition.getReplicas().stream().map(Replica::getBrokerId).collect(Collectors.toSet()), hasSize(2));
    }
}
