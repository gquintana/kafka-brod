package com.github.gquintana.kafka.brod.consumer;

import com.github.gquintana.kafka.brod.KafkaService;
import kafka.admin.AdminClient;
import kafka.coordinator.group.GroupOverview;
import kafka.coordinator.group.MemberSummary;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import scala.Option;
import scala.collection.JavaConversions;

import java.util.*;
import java.util.stream.Collectors;

public class ConsumerGroupService {
    private static final long TIMEOUT_MS = 10000L;
    private final KafkaService kafkaService;

    public ConsumerGroupService(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    private Collection<GroupOverview> getGroupOverviews() {
        scala.collection.immutable.List<GroupOverview> groupOverviews = kafkaService.scalaAdminClient().listAllConsumerGroupsFlattened();
        return JavaConversions.asJavaCollection(groupOverviews);
    }

    public List<String> getGroupIds() {
        return getGroupOverviews().stream()
            .map(cgo -> cgo.groupId())
            .collect(Collectors.toList());
    }


    public List<String> getGroupIds(int brokerId) {
        scala.collection.immutable.Map<Node, scala.collection.immutable.List<GroupOverview>> groupsByNode = kafkaService.scalaAdminClient().listAllConsumerGroups();
        return JavaConversions.mapAsJavaMap(groupsByNode).entrySet().stream()
            .filter(e -> e.getKey().id() == brokerId)
            .flatMap(e -> JavaConversions.asJavaCollection(e.getValue()).stream())
            .map(e -> e.groupId())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    private com.github.gquintana.kafka.brod.consumer.Consumer convertToJson(MemberSummary member) {
        com.github.gquintana.kafka.brod.consumer.Consumer memberJson = new com.github.gquintana.kafka.brod.consumer.Consumer();
        memberJson.setClientId(member.clientId());
        memberJson.setClientHost(member.clientHost());
        memberJson.setMemberId(member.memberId());
        return memberJson;
    }

    private AdminClient.ConsumerGroupSummary getGroupSummary(String groupId) {
        return kafkaService.scalaAdminClient().describeConsumerGroup(groupId, TIMEOUT_MS);
    }

    private List<com.github.gquintana.kafka.brod.consumer.Consumer> getConsumers(AdminClient.ConsumerGroupSummary groupSummary, String topic) {
        Option<scala.collection.immutable.List<AdminClient.ConsumerSummary>> consumerSummaries =
            groupSummary.consumers();
        if (consumerSummaries.isEmpty()) {
            return Collections.emptyList();
        }
        return JavaConversions.asJavaCollection(groupSummary.consumers().get())
            .stream()
            .map(c -> convertToJson(c, topic))
            .sorted(Comparator.comparing(com.github.gquintana.kafka.brod.consumer.Consumer::getMemberId))
            .collect(Collectors.toList());
    }

    public Optional<ConsumerGroup> getGroup(String groupId) {
        return getGroup(groupId, null);
    }

    /**
     *
     * @param topic Optional topic name to filter assignments
     */
    public Optional<ConsumerGroup> getGroup(String groupId, String topic) {
        AdminClient.ConsumerGroupSummary groupSummary = getGroupSummary(groupId);
        ConsumerGroup group = convertToJson(groupId, groupSummary, topic);
        List<ConsumerPartition> partitions = group.getMembers().stream().flatMap(m -> m.getPartitions().stream()).collect(Collectors.toList());
        getGroupOffset(groupId, partitions);
        return Optional.of(group);
    }

    private ConsumerGroup convertToJson(String groupId, AdminClient.ConsumerGroupSummary groupSummary, String topic) {
        ConsumerGroup group = new ConsumerGroup();
        group.setGroupId(groupId);
        group.setProtocol(groupSummary.productPrefix());
        group.setState(groupSummary.state());
        group.setAssignmentStrategy(groupSummary.assignmentStrategy());
        group.setMembers(getConsumers(groupSummary, topic));
        return group;
    }

    private com.github.gquintana.kafka.brod.consumer.Consumer convertToJson(AdminClient.ConsumerSummary consumerSummary) {
        return convertToJson(consumerSummary, null);
    }

    private com.github.gquintana.kafka.brod.consumer.Consumer convertToJson(AdminClient.ConsumerSummary consumerSummary, String topic) {
        com.github.gquintana.kafka.brod.consumer.Consumer member = new com.github.gquintana.kafka.brod.consumer.Consumer();
        member.setClientId(consumerSummary.clientId());
        member.setClientHost(consumerSummary.host());
        member.setMemberId(consumerSummary.consumerId());
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

    public void getGroupOffset(String groupId, List<ConsumerPartition> partitions) {
        try(Consumer<String, String> consumer = kafkaService.consumer(groupId)) {
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
            partition.setCommitedOffset(offsetAndMetadata.offset());
            partition.setMetadata(offsetAndMetadata.metadata());
        }
        partition.setCurrentOffset(position);
    }
}
