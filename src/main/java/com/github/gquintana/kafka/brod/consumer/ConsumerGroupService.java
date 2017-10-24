package com.github.gquintana.kafka.brod.consumer;

import com.github.gquintana.kafka.brod.KafkaService;
import kafka.admin.AdminClient;
import kafka.coordinator.group.GroupOverview;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static scala.collection.JavaConverters.*;

import java.util.*;

public class ConsumerGroupService {
    private static final long TIMEOUT_MS = 10000L;
    private final KafkaService kafkaService;

    public ConsumerGroupService(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Consumer group list

    public List<String> getGroupIds() {
        return getGroupOverviews().stream()
            .map(cgo -> cgo.groupId())
            .collect(toList());
    }


    public List<String> getGroupIds(int brokerId) {
        scala.collection.immutable.Map<Node, scala.collection.immutable.List<GroupOverview>> groupsByNode = kafkaService.scalaAdminClient().listAllConsumerGroups();
        return mapAsJavaMap(groupsByNode).entrySet().stream()
            .filter(e -> e.getKey().id() == brokerId)
            .flatMap(e -> asJavaCollection(e.getValue()).stream())
            .map(e -> e.groupId())
            .distinct()
            .sorted()
            .collect(toList());
    }

    private Collection<GroupOverview> getGroupOverviews() {
        scala.collection.immutable.List<GroupOverview> groupOverviews = kafkaService.scalaAdminClient().listAllConsumerGroupsFlattened();
        return asJavaCollection(groupOverviews);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Consumer group detail

    public Optional<ConsumerGroup> getGroup(String groupId) {
        return getGroup(groupId, null);
    }

    /**
     *
     * @param topic Optional topic name to filter assignments
     */
    public Optional<ConsumerGroup> getGroup(String groupId, String topic) {
        // Group
        AdminClient.ConsumerGroupSummary groupSummary = getGroupSummary(groupId);
        ConsumerGroup group = convertToConsumerGroup(groupId, groupSummary);
        Collection<AdminClient.ConsumerSummary> consumerSummaries = groupSummary.consumers().isDefined()?
            asJavaCollection(groupSummary.consumers().get()):
            Collections.emptyList();
        // Partitions
        List<TopicPartition> partitions = getPartitions(consumerSummaries, topic);
        Map<TopicPartition, Long> consumerOffsets = getGroupOffsets(groupId);
        Map<TopicPartition, Long> topicOffsets = getPartitionOffset(groupId, partitions);
        Map<TopicPartition, ConsumerPartition> consumerPartitions = partitions.stream()
            .collect(toMap((tp) -> tp, (tp) -> convertToConsumerPartition(tp, consumerOffsets, topicOffsets)));
        // Members
        group.setMembers(convertToConsumers(consumerSummaries, consumerPartitions));
        return Optional.of(group);
    }

    private AdminClient.ConsumerGroupSummary getGroupSummary(String groupId) {
        return kafkaService.scalaAdminClient().describeConsumerGroup(groupId, TIMEOUT_MS);
    }

    private List<TopicPartition> getPartitions(Collection<AdminClient.ConsumerSummary> consumerSummaries, String topic) {
        return consumerSummaries.stream()
            .flatMap(cs -> asJavaCollection(cs.assignment()).stream())
            .filter(p -> topic == null || topic.equals(p.topic()))
            .collect(toList());
    }

    private Map<TopicPartition, Long> getGroupOffsets(String groupId) {
        return mapAsJavaMap(kafkaService.scalaAdminClient().listGroupOffsets(groupId).mapValues(l -> (Long) l));
    }

    private ConsumerGroup convertToConsumerGroup(String groupId, AdminClient.ConsumerGroupSummary groupSummary) {
        ConsumerGroup group = new ConsumerGroup(groupId);
        group.setProtocol(groupSummary.productPrefix());
        group.setState(groupSummary.state());
        group.setAssignmentStrategy(groupSummary.assignmentStrategy());
        return group;
    }

    private List<com.github.gquintana.kafka.brod.consumer.Consumer> convertToConsumers(Collection<AdminClient.ConsumerSummary> consumerSummaries, Map<TopicPartition, ConsumerPartition> consumerPartitions) {
        return consumerSummaries
            .stream()
            .map(c -> convertToConsumer(c, consumerPartitions))
            .sorted(Comparator.comparing(com.github.gquintana.kafka.brod.consumer.Consumer::getMemberId))
            .collect(toList());
    }

    private com.github.gquintana.kafka.brod.consumer.Consumer convertToConsumer(AdminClient.ConsumerSummary consumerSummary, Map<TopicPartition, ConsumerPartition> consumerPartitions) {
        com.github.gquintana.kafka.brod.consumer.Consumer member = new com.github.gquintana.kafka.brod.consumer.Consumer();
        member.setClientId(consumerSummary.clientId());
        member.setClientHost(consumerSummary.host());
        member.setMemberId(consumerSummary.consumerId());
        member.setPartitions(asJavaCollection(consumerSummary.assignment()).stream()
            .map(p -> consumerPartitions.get(p))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(ConsumerPartition::getTopicName).thenComparing(ConsumerPartition::getId))
            .collect(toList()));
        return member;
    }

    private ConsumerPartition convertToConsumerPartition(TopicPartition topicPartition, Map<TopicPartition, Long> consumerOffsets, Map<TopicPartition, Long> topicOffsets) {
        ConsumerPartition partition = new ConsumerPartition(topicPartition.topic(), topicPartition.partition());
        partition.setCommitedOffset(consumerOffsets.get(topicPartition));
        partition.setEndOffset(topicOffsets.get(topicPartition));
        return partition;
    }

    private Map<TopicPartition, Long> getPartitionOffset(String groupId, List<TopicPartition> partitions) {
        try(Consumer<String, String> consumer = kafkaService.consumer(groupId)) {
            return consumer.endOffsets(partitions);
        }
    }
}
