package com.github.gquintana.kafka.brod.topic;

import com.github.gquintana.kafka.brod.KafkaService;
import com.github.gquintana.kafka.brod.ZookeeperService;
import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.server.ConfigType;
import kafka.utils.ZkUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.requests.MetadataResponse;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class TopicServiceImpl implements TopicService {
    private final ZookeeperService zookeeperService;
    private final KafkaService kafkaService;

    public TopicServiceImpl(ZookeeperService zookeeperService, KafkaService kafkaService) {
        this.zookeeperService = zookeeperService;
        this.kafkaService = kafkaService;
    }

    private ZkUtils getZkUtils() {
        return zookeeperService.getZkUtils();
    }

    @Override
    public void createTopic(Topic topic) {
        AdminUtils.createTopic(getZkUtils(), topic.getName(), topic.getNumPartitions(), topic.getReplicationFactor(), topic.getConfig(), RackAwareMode.Enforced$.MODULE$);
    }

    @Override
    public void deleteTopic(String name) {
        AdminUtils.deleteTopic(getZkUtils(), name);
    }

    @Override
    public void updateTopic(Topic topic) {
        AdminUtils.changeTopicConfig(getZkUtils(), topic.getName(), topic.getConfig());
    }

    @Override
    public Optional<Topic> getTopic(String name) {
        if (!existTopic(name)) {
            return Optional.empty();
        }
        Properties config = AdminUtils.fetchEntityConfig(getZkUtils(), ConfigType.Topic(), name);
        MetadataResponse.TopicMetadata topicMetadata = AdminUtils.fetchTopicMetadataFromZk(name, getZkUtils());
        List<MetadataResponse.PartitionMetadata> partitionMetadata = topicMetadata.partitionMetadata();
        int numPartitions = partitionMetadata.stream().mapToInt(MetadataResponse.PartitionMetadata::partition).max().orElse(-1) + 1;
        int replicationFactor = partitionMetadata.stream().mapToInt(p -> p.replicas().size()).max().orElse(0);
        // Partition offsets
        List<Partition> partitions = convertToPartitions(name, partitionMetadata);
        return Optional.of(new Topic(name, numPartitions, replicationFactor, topicMetadata.isInternal(), config, partitions));
    }

    @Override
    public boolean existTopic(String name) {
        return AdminUtils.topicExists(getZkUtils(), name);
    }

    @Override
    public List<String> getTopics() {
        return zookeeperService.getChildren("/brokers/topics").stream().sorted().collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Topic partitions

    private List<Partition> convertToPartitions(String name, List<MetadataResponse.PartitionMetadata> partitionMetadata) {
        List<TopicPartition> topicPartitions = partitionMetadata.stream()
            .map(p -> new TopicPartition(name, p.partition())).collect(toList());
        Map<TopicPartition, PartitionOffsets> partitionOffsets = getPartitionOffsets(topicPartitions);
        return partitionMetadata.stream()
            .map(p -> convertToPartition(name, p, partitionOffsets))
            .collect(Collectors.toList());
    }

    private static Partition convertToPartition(String topic, MetadataResponse.PartitionMetadata partitionMetadata, Map<TopicPartition, PartitionOffsets> partitionOffsets) {
        final Set<Integer> inSyncBrokers = partitionMetadata.isr().stream().map(Node::id).collect(Collectors.toSet());
        int leaderBroker = partitionMetadata.leader().id();
        List<Replica> replicas = partitionMetadata.replicas().stream()
            .map(r -> convertToReplica(r, inSyncBrokers, leaderBroker))
            .sorted(Comparator.comparing(Replica::getBrokerId))
            .collect(Collectors.toList());
        PartitionOffsets partitionOffsets1 = partitionOffsets.get(new TopicPartition(topic, partitionMetadata.partition()));
        Long beginningOffset = null;
        Long endOffset = null;
        if (partitionOffsets1 != null) {
            beginningOffset = partitionOffsets1.getBeginningOffset();
            endOffset = partitionOffsets1.getEndOffset();
        }
        return new Partition(topic, partitionMetadata.partition(), beginningOffset, endOffset, replicas);
    }

    private static Replica convertToReplica(Node replica, Set<Integer> inSyncBrokers, int leaderBroker) {
        int broker = replica.id();
        return new Replica(broker, leaderBroker == broker, inSyncBrokers.contains(broker));
    }

    private static class PartitionOffsets {
        private final TopicPartition partition;
        private final Long beginOffset;
        private final Long endOffset;

        private PartitionOffsets(TopicPartition partition, Long beginningOffset, Long endOffset) {
            this.partition = partition;
            this.beginOffset = beginningOffset;
            this.endOffset = endOffset;
        }

        public TopicPartition getPartition() {
            return partition;
        }

        public Long getBeginningOffset() {
            return beginOffset;
        }

        public Long getEndOffset() {
            return endOffset;
        }
    }

    private Map<TopicPartition, PartitionOffsets> getPartitionOffsets(List<TopicPartition> partitions) {
        try (Consumer<String, String> consumer = kafkaService.consumer("topic_partition_service")) {
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);
            Map<TopicPartition, Long> beginningOffsets = consumer.beginningOffsets(partitions);
            return partitions.stream().map(p -> new PartitionOffsets(p, beginningOffsets.get(p), endOffsets.get(p))).collect(toMap(PartitionOffsets::getPartition, p -> p));
        }
    }

}
