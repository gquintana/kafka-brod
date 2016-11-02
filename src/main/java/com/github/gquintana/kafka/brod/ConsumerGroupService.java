package com.github.gquintana.kafka.brod;

import kafka.admin.AdminClient;
import kafka.coordinator.GroupOverview;
import kafka.coordinator.GroupSummary;
import kafka.coordinator.MemberSummary;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import scala.Option;
import scala.Predef;
import scala.collection.JavaConversions;

import java.util.*;
import java.util.stream.Collectors;

public class ConsumerGroupService implements AutoCloseable {
    private final String bootstrapServers;
    private AdminClient adminClient;

    public ConsumerGroupService(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public AdminClient adminClient() {
        if (adminClient == null) {
            Map<String, Object> brokerConfig = new HashMap<>();
            brokerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            adminClient = AdminClient.create(JavaConversions.mapAsScalaMap(brokerConfig).toMap(Predef.conforms()));
        }
        return adminClient;
    }

    private Collection<GroupOverview> getGroupOverviews() {
        scala.collection.immutable.List<GroupOverview> groupOverviews = adminClient().listAllConsumerGroupsFlattened();
        return JavaConversions.asJavaCollection(groupOverviews);
    }

    public List<String> getGroupIds() {
        return getGroupOverviews().stream()
            .map(cgo -> cgo.groupId())
            .collect(Collectors.toList());
    }


    public List<String> getGroupIds(int brokerId) {
        scala.collection.immutable.Map<Node, scala.collection.immutable.List<GroupOverview>> groupsByNode = adminClient().listAllConsumerGroups();
        return JavaConversions.mapAsJavaMap(groupsByNode).entrySet().stream()
            .filter(e -> e.getKey().id() == brokerId)
            .flatMap(e -> JavaConversions.asJavaCollection(e.getValue()).stream())
            .map(e -> e.groupId())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    private com.github.gquintana.kafka.brod.Consumer convertToJson(MemberSummary member) {
        com.github.gquintana.kafka.brod.Consumer memberJson = new com.github.gquintana.kafka.brod.Consumer();
        memberJson.setClientId(member.clientId());
        memberJson.setClientHost(member.clientHost());
        memberJson.setMemberId(member.memberId());
        return memberJson;
    }

    private GroupSummary getGroupSummary(String groupId) {
        return adminClient().describeGroup(groupId);
    }

    private Collection<AdminClient.ConsumerSummary> getConsumerSummaries(String groupId) {
        Option<scala.collection.immutable.List<AdminClient.ConsumerSummary>> consumerSummaries = adminClient().describeConsumerGroup(groupId);
        if (consumerSummaries.isEmpty()) {
            return Collections.emptyList();
        } else {
            return JavaConversions.asJavaCollection(consumerSummaries.get());
        }
    }

    public Optional<ConsumerGroup> getGroup(String groupId) {
        return getGroup(groupId, null);
    }

    /**
     *
     * @param topic Optional topic name to filter assignments
     */
    public Optional<ConsumerGroup> getGroup(String groupId, String topic) {
        GroupSummary groupSummary = getGroupSummary(groupId);
        if ("dead".equalsIgnoreCase(groupSummary.state())) {
            return Optional.empty();
        }
        ConsumerGroup group = convertToJson(groupId, groupSummary);
        List<com.github.gquintana.kafka.brod.Consumer> consumers = getConsumerSummaries(groupId).stream()
            .map(c -> convertToJson(c, topic))
            .sorted(Comparator.comparing(com.github.gquintana.kafka.brod.Consumer::getMemberId))
            .collect(Collectors.toList());
        group.setMembers(consumers);
        List<ConsumerPartition> partitions = group.getMembers().stream().flatMap(m -> m.getPartitions().stream()).collect(Collectors.toList());
        getGroupOffset(groupId, partitions);
        return Optional.of(group);
    }

    private ConsumerGroup convertToJson(String groupId, GroupSummary groupSummary) {
        ConsumerGroup group = new ConsumerGroup();
        group.setGroupId(groupId);
        group.setProtocol(groupSummary.protocol());
        group.setState(groupSummary.state());
        group.setMembers(JavaConversions.asJavaCollection(groupSummary.members()).stream()
            .map(this::convertToJson)
            .sorted(Comparator.comparing(com.github.gquintana.kafka.brod.Consumer::getMemberId))
            .collect(Collectors.toList()));
        return group;
    }

    private com.github.gquintana.kafka.brod.Consumer convertToJson(AdminClient.ConsumerSummary consumerSummary, String topic) {
        com.github.gquintana.kafka.brod.Consumer member = new com.github.gquintana.kafka.brod.Consumer();
        member.setClientId(consumerSummary.clientId());
        member.setClientHost(consumerSummary.clientHost());
        member.setMemberId(consumerSummary.memberId());
        member.setPartitions(JavaConversions.asJavaCollection(consumerSummary.assignment()).stream()
            .filter(tp -> topic == null || tp.topic().equals(topic))
            .map(this::convertToJson)
            .sorted(Comparator.comparing(ConsumerPartition::getTopicName).thenComparing(ConsumerPartition::getId))
            .collect(Collectors.toList()));
        return member;
    }


    private ConsumerPartition convertToJson(TopicPartition topicPartition) {
        ConsumerPartition partition = new ConsumerPartition();
        partition.setTopicName(topicPartition.topic());
        partition.setId(topicPartition.partition());
        return partition;
    }

    private Consumer<String, String> createConsumer(String groupId) {
        Map<String, Object> consumerConfig = new HashMap<>();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerConfig.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return new KafkaConsumer<>(consumerConfig);
    }

    public void getGroupOffset(String groupId, List<ConsumerPartition> partitions) {
        try(Consumer<String, String> consumer = createConsumer(groupId)) {
            List<TopicPartition> topicPartitions = partitions.stream().map(p -> new TopicPartition(p.getTopicName(), p.getId())).collect(Collectors.toList());
            consumer.assign(topicPartitions);
            consumer.seekToEnd(topicPartitions);
            partitions.stream().forEach(p -> enrichPartitionWithOffsets(p, consumer));
        }
    }

    private static void enrichPartitionWithOffsets(ConsumerPartition partition, Consumer consumer) {
        TopicPartition topicPartition = new TopicPartition(partition.getTopicName(), partition.getId());
        OffsetAndMetadata offsetAndMetadata = consumer.committed(topicPartition);
        long position = consumer.position(topicPartition);
        if (offsetAndMetadata !=null) {
            partition.setTopicOffset(offsetAndMetadata.offset());
            partition.setMetadata(offsetAndMetadata.metadata());
        }
        partition.setConsumerOffset(position);
    }

    @Override
    public void close() throws Exception {
        if (adminClient != null) {
            adminClient.close();
        }
    }
}
