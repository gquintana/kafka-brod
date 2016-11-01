package com.github.gquintana.kafka.brod;

import kafka.admin.AdminClient;
import kafka.coordinator.GroupOverview;
import kafka.coordinator.GroupSummary;
import kafka.coordinator.MemberSummary;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import scala.Option;
import scala.Predef;
import scala.collection.JavaConversions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public List<String> getGroupIds() {
        scala.collection.immutable.List<GroupOverview> groupOverviews = adminClient().listAllConsumerGroupsFlattened();
        return JavaConversions.asJavaCollection(groupOverviews).stream()
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
            .collect(Collectors.toList());
    }

    public Optional<ConsumerGroup> getGroup(String groupId) {
        GroupSummary groupSummary = adminClient().describeGroup(groupId);
        if ("Dead".equalsIgnoreCase(groupSummary.state())) {
            return Optional.empty();
        }
        ConsumerGroup group = convertToJson(groupId, groupSummary);
        return Optional.of(group);
    }

    private ConsumerGroup convertToJson(String groupId, GroupSummary groupSummary) {
        ConsumerGroup group = new ConsumerGroup();
        group.setGroupId(groupId);
        group.setProtocol(groupSummary.protocol());
        group.setState(groupSummary.state());
        group.setMembers(JavaConversions.asJavaCollection(groupSummary.members()).stream()
            .map(this::convertToJson).collect(Collectors.toList()));
        return group;
    }

    private ConsumerGroup.Member convertToJson(MemberSummary member) {
        ConsumerGroup.Member memberJson = new ConsumerGroup.Member();
        memberJson.setClientId(member.clientId());
        memberJson.setClientHost(member.clientHost());
        memberJson.setMemberId(member.memberId());
        return memberJson;
    }

    public Optional<ConsumerGroup> getGroupWithAssignments(String groupId) {
        Option<scala.collection.immutable.List<AdminClient.ConsumerSummary>> consumerSummaries = adminClient().describeConsumerGroup(groupId);
        if (consumerSummaries.isEmpty()) {
            return Optional.empty();
        }
        List<ConsumerGroup.Member> consumers = JavaConversions.asJavaCollection(consumerSummaries.get()).stream()
            .map(this::convertToJson)
            .collect(Collectors.toList());
        ConsumerGroup group = new ConsumerGroup();
        group.setGroupId(groupId);
        group.setMembers(consumers);
        return Optional.of(group);
    }

    private ConsumerGroup.Member convertToJson(AdminClient.ConsumerSummary consumerSummary) {
        ConsumerGroup.Member member = new ConsumerGroup.Member();
        member.setClientId(consumerSummary.clientId());
        member.setClientHost(consumerSummary.clientHost());
        member.setMemberId(consumerSummary.memberId());
        member.setPartitions(JavaConversions.asJavaCollection(consumerSummary.assignment()).stream()
            .map(this::convertToJson)
            .collect(Collectors.toList()));
        return member;
    }


    private Partition convertToJson(TopicPartition topicPartition) {
        Partition partition = new Partition();
        partition.setTopicName(topicPartition.topic());
        partition.setId(topicPartition.partition());
        return partition;
    }


    @Override
    public void close() throws Exception {
        if (adminClient != null) {
            adminClient.close();
        }
    }
}
