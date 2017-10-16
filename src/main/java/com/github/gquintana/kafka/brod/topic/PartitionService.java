package com.github.gquintana.kafka.brod.topic;

import com.github.gquintana.kafka.brod.KafkaService;
import com.github.gquintana.kafka.brod.ZookeeperService;
import kafka.admin.AdminUtils;
import kafka.utils.ZkUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.requests.MetadataResponse;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Partition info service
 */
public class PartitionService {
    private final ZookeeperService zookeeperService;
    private final KafkaService kafkaService;

    public PartitionService(ZookeeperService zookeeperService, KafkaService kafkaService) {
        this.zookeeperService = zookeeperService;
        this.kafkaService = kafkaService;
    }

    private ZkUtils getZkUtils() {
        return zookeeperService.getZkUtils();
    }


    public List<Partition> getPartitions(String topic) {
        if (!AdminUtils.topicExists(getZkUtils(), topic)) {
            return Collections.emptyList();
        }
        MetadataResponse.TopicMetadata topicMetadata = AdminUtils.fetchTopicMetadataFromZk(topic, getZkUtils());
        List<TopicPartition> topicPartitions = topicMetadata.partitionMetadata().stream().map(p -> new TopicPartition(topic, p.partition())).collect(toList());
        Map<TopicPartition, PartitionOffsets> partitionOffsets = getPartitionOffsets(topicPartitions);
        return topicMetadata.partitionMetadata().stream().map(p -> convertToPartition(topic, p, partitionOffsets)).collect(Collectors.toList());
    }

    public Optional<Partition> getPartition(String topic, int partition) {
        if (!AdminUtils.topicExists(getZkUtils(), topic)) {
            return Optional.empty();
        }
        MetadataResponse.TopicMetadata topicMetadata = AdminUtils.fetchTopicMetadataFromZk(topic, getZkUtils());
        List<TopicPartition> topicPartitions = Collections.singletonList(new TopicPartition(topic, partition));
        Map<TopicPartition, PartitionOffsets> partitionOffsets = getPartitionOffsets(topicPartitions);
        return topicMetadata.partitionMetadata().stream().filter(p -> p.partition() == partition).findFirst().map(p -> convertToPartition(topic, p, partitionOffsets));
    }


    private static Partition convertToPartition(String topic,MetadataResponse.PartitionMetadata partitionMetadata, Map<TopicPartition, PartitionOffsets> partitionOffsets) {
        Partition partition = new Partition(topic, partitionMetadata.partition());
        final Set<Integer> inSyncBrokers = partitionMetadata.isr().stream().map(Node::id).collect(Collectors.toSet());
        int leaderBroker = partitionMetadata.leader().id();
        List<Replica> replicas = partitionMetadata.replicas().stream()
            .map(r -> convertToReplica(r, inSyncBrokers, leaderBroker))
            .sorted(Comparator.comparing(Replica::getBrokerId))
            .collect(Collectors.toList());
        partition.setReplicas(replicas);
        PartitionOffsets partitionOffsets1 = partitionOffsets.get(new TopicPartition(topic, partitionMetadata.partition()));
        if (partitionOffsets1 != null) {
            partition.setBeginningOffset(partitionOffsets1.getBeginningOffset());
            partition.setEndOffset(partitionOffsets1.getEndOffset());
        }
        return partition;
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
        try(Consumer<String, String> consumer = kafkaService.consumer("topic_partition_service")) {
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);
            Map<TopicPartition, Long> beginningOffsets = consumer.beginningOffsets(partitions);
            return partitions.stream().map(p -> new PartitionOffsets(p, beginningOffsets.get(p), endOffsets.get(p))).collect(toMap(PartitionOffsets::getPartition, p -> p));
        }
    }


}
