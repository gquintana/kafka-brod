package com.github.gquintana.kafka.brod.consumer;

import com.github.gquintana.kafka.brod.EmbeddedKafkaRule;
import com.github.gquintana.kafka.brod.KafkaService;
import com.github.gquintana.kafka.brod.ZookeeperService;
import com.github.gquintana.kafka.brod.topic.TopicService;
import com.github.gquintana.kafka.brod.topic.TopicServiceImpl;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ConsumerGroupServiceImplTest {
    @ClassRule
    public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

    @ClassRule
    public static final EmbeddedKafkaRule KAFKA_RULE = new EmbeddedKafkaRule(TEMPORARY_FOLDER);
    private static final String TOPIC = "test_group";
    private static final String TOPIC2 = "test_group2";
    private static final int PARTITIONS = 3;
    private ConsumerGroupService groupService;
    private ZookeeperService zookeeperService;
    private TopicService topicService;
    private ExecutorService executor = Executors.newFixedThreadPool(6);
    private KafkaService kafkaService;
    private List<TopicConsumerRunnable> runnables = new ArrayList<>();

    @BeforeClass
    public static void setUpClass() throws ExecutionException, InterruptedException {
        KAFKA_RULE.getKafka().createTopic(TOPIC, PARTITIONS, 1);
        KAFKA_RULE.getKafka().createTopic(TOPIC2, PARTITIONS, 1);
    }

    @Before
    public void setUp() throws IOException {
        zookeeperService = new ZookeeperService("localhost:2181", 3000, 3000);
        kafkaService = new KafkaService("localhost:9092", "kafka-brod");
        topicService = new TopicServiceImpl(zookeeperService, kafkaService);
        groupService = new ConsumerGroupServiceImpl(kafkaService);
    }

    private Consumer startConsumer(String groupId, String ... topics) throws InterruptedException {
        Consumer<Long, String> consumer = (Consumer<Long, String>) KAFKA_RULE.getKafka().createConsumer(groupId);
        TopicConsumerListener listener = new TopicConsumerListener();
        TopicConsumerRunnable runnable = new TopicConsumerRunnable(groupId, topics, listener);
        executor.execute(runnable);
        runnables.add(runnable);
        listener.waitPartitionsAssigned();
        return consumer;
    }

    @Test
    public void testGetGroupIds() throws Exception {
        // Given
        startConsumer("get_group_id_1", TOPIC);
        startConsumer("get_group_id_1", TOPIC);
        startConsumer("get_group_id_2", TOPIC);
        // When
        List<String> groupIds = groupService.getGroupIds();
        // Then
        assertThat(groupIds, hasItems("get_group_id_1", "get_group_id_2"));
    }

    @Test
    public void testGetGroup() throws Exception {
        // Given
        startConsumer("get_group", TOPIC);
        startConsumer("get_group", TOPIC);
        // When
        Optional<ConsumerGroup> group = groupService.getGroup("get_group");
        // Then
        assertThat(group.isPresent(), is(true));
        assertThat(group.get().getId(), equalTo("get_group"));
        assertThat(group.get().getState(), equalTo("Stable"));
        List<com.github.gquintana.kafka.brod.consumer.Consumer> members = group.get().getMembers();
        assertThat(members.size(), is(2));
        assertThat(members.stream().map(com.github.gquintana.kafka.brod.consumer.Consumer::getId).distinct().collect(toList()).size(), is(2));
        assertThat(members.stream().map(com.github.gquintana.kafka.brod.consumer.Consumer::getClientId).distinct().collect(toList()).size(), is(2));
        assertThat(members.stream().map(com.github.gquintana.kafka.brod.consumer.Consumer::getClientHost).distinct().collect(toList()).size(), is(1));
        assertThat(members.stream().flatMap(m -> m.getPartitions().stream()).collect(toList()).size(), is(3));
    }

    @Test
    public void testGetGroup_ByTopic() throws Exception {
        // Given
        startConsumer("get_group_by_topic", TOPIC, TOPIC2);
        // When
        Optional<ConsumerGroup> group = groupService.getGroup("get_group_by_topic");
        // Then
        assertThat(group.isPresent(), is(true));
        assertThat(group.get().getId(), equalTo("get_group_by_topic"));
        List<com.github.gquintana.kafka.brod.consumer.Consumer> members = group.get().getMembers();
        assertThat(members.size(), is(1));
        assertThat(members.stream().flatMap(m -> m.getPartitions().stream()).collect(toList()).size(), is(3*2));
        // When
        group = groupService.getGroup("get_group_by_topic", TOPIC);
        // Then
        assertThat(group.isPresent(), is(true));
        assertThat(group.get().getId(), equalTo("get_group_by_topic"));
        members = group.get().getMembers();
        assertThat(members.size(), is(1));
        assertThat(members.stream().flatMap(m -> m.getPartitions().stream()).collect(toList()).size(), is(3*1));
    }

    @Test
    public void testGetConsumer() throws Exception {
        // Given
        String groupId = "get_consumer";
        startConsumer(groupId, TOPIC);
        // When
        Optional<ConsumerGroup> group = groupService.getGroup(groupId);
        String consumerId = group.get().getMembers().get(0).getId();
        assertNotNull(consumerId);
        Optional<com.github.gquintana.kafka.brod.consumer.Consumer> consumer= groupService.getConsumer(groupId, consumerId, null);
        // Then
        assertThat(consumer.isPresent(), is(true));
        assertThat(consumer.get().getId(), equalTo(consumerId));
        assertThat(consumer.get().getPartitions().size(), is(3));
        assertThat(consumer.get().getPartitions().stream().map(ConsumerPartition::getTopicName).collect(toSet()).size(), is(1));
    }

    @After
    public void tearDown() {
        runnables.stream().forEach(TopicConsumerRunnable::stop);
        runnables.clear();
        executor.shutdown();
        kafkaService.close();
        zookeeperService.close();
    }

    private static class TopicConsumerRunnable implements Runnable {
        private final AtomicBoolean running = new AtomicBoolean(true);
        private final TopicConsumerListener listener;
        private final Consumer<Long, String> consumer;

        public TopicConsumerRunnable(String groupId, String[] topics, TopicConsumerListener listener) {
            this.listener = listener;
            consumer = (Consumer<Long, String>) KAFKA_RULE.getKafka().createConsumer(groupId);
            consumer.subscribe(Arrays.asList(topics), this.listener);
        }

        @Override
        public void run() {
            try {
                while (running.get()) {
                    ConsumerRecords<Long, String> records = consumer.poll(1000L);
                    for (ConsumerRecord<Long, String> record: records) {
                        listener.onRecord(record);
                    }
                }
            } finally {
                consumer.close();
            }
        }

        public void stop() {
            running.set(false);
        }
    }

    private static class TopicConsumerListener implements ConsumerRebalanceListener {
        private final CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> collection) {

        }

        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            if (latch.getCount() > 0) {
                if (partitions.stream().filter(p -> p.topic().equals(TOPIC)).findAny().isPresent()) {
                    latch.countDown();
                }
            }
        }

        public void waitPartitionsAssigned() throws InterruptedException {
            if (latch.getCount() > 0) {
                latch.await();
            }
        }

        public void onRecord(ConsumerRecord<Long, String> record) {

        }
    }
}
