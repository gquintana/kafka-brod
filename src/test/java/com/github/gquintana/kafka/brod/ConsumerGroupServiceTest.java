package com.github.gquintana.kafka.brod;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ConsumerGroupServiceTest {
    @ClassRule
    public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

    @ClassRule
    public static final EmbeddedKafkaRule KAFKA_RULE = new EmbeddedKafkaRule(TEMPORARY_FOLDER);
    private static final String TOPIC = "test_group";
    private static final int PARTITIONS = 2;
    private static ConsumerGroupService groupService;
    private static ZookeeperService zookeeperService;
    private static TopicService topicService;
    private static ExecutorService executor = Executors.newFixedThreadPool(6);
    private List<TopicConsumerRunnable> runnables = new ArrayList<>();

    @BeforeClass
    public static void setUpClass() throws IOException {
        zookeeperService = new ZookeeperService("localhost:2181", 3000, 3000);
        topicService = new TopicService(zookeeperService);
        topicService.createTopic(new Topic(TOPIC, PARTITIONS, 1, new Properties()));
        groupService = new ConsumerGroupService("localhost:9092");
    }

    private Consumer startConsumer(String groupId) throws InterruptedException {
        Consumer<Long, String> consumer = KAFKA_RULE.getKafka().createConsumer(groupId);
        TopicConsumerListener listener = new TopicConsumerListener();
        TopicConsumerRunnable runnable = new TopicConsumerRunnable(groupId, TOPIC, listener);
        executor.execute(runnable);
        listener.waitPartitionsAssigned();
        return consumer;
    }

    @Test
    public void testGetGroupIds() throws Exception {
        // Given
        startConsumer("get_group_id_1");
        startConsumer("get_group_id_1");
        startConsumer("get_group_id_2");
        // When
        List<String> groupIds = groupService.getGroupIds();
        // Then
        assertThat(groupIds, hasItems("get_group_id_1", "get_group_id_2"));
    }

    @Test
    public void testGetGroup() throws Exception {
        // Given
        startConsumer("get_group");
        startConsumer("get_group");
        // When
        Optional<ConsumerGroup> group = groupService.getGroup("get_group");
        // Then
        assertThat(group.isPresent(), is(true));
        assertThat(group.get().getGroupId(), equalTo("get_group"));
        assertThat(group.get().getState(), equalTo("Stable"));
        List<ConsumerGroup.Member> members = group.get().getMembers();
        assertThat(members.size(), is(2));
        assertThat(members.stream().map(ConsumerGroup.Member::getMemberId).distinct().collect(toList()).size(), is(2));
        assertThat(members.stream().map(ConsumerGroup.Member::getClientId).distinct().collect(toList()).size(), is(2));
        assertThat(members.stream().map(ConsumerGroup.Member::getClientHost).distinct().collect(toList()).size(), is(1));
    }

    @After
    public void tearDown() {
        runnables.stream().forEach(TopicConsumerRunnable::stop);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        executor.shutdown();
        groupService.close();
        zookeeperService.close();
    }

    private static class TopicConsumerRunnable implements Runnable {
        private final AtomicBoolean running = new AtomicBoolean(true);
        private final TopicConsumerListener listener;
        private final Consumer<Long, String> consumer;

        public TopicConsumerRunnable(String groupId, String topic, TopicConsumerListener listener) {
            this.listener = listener;
            consumer = KAFKA_RULE.getKafka().createConsumer(groupId);
            consumer.subscribe(Collections.singletonList(topic), this.listener);
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
            consumer.close();
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
