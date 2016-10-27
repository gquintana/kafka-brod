package com.github.gquintana.kafka.brod;

import kafka.admin.AdminUtils;
import kafka.utils.ZkUtils;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.requests.MetadataResponse;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Partition info service
 */
public class PartitionService {
    private final ZookeeperService zookeeperService;

    public PartitionService(ZookeeperService zookeeperService) {
        this.zookeeperService = zookeeperService;
    }

    private ZkUtils getZkUtils() {
        return zookeeperService.getZkUtils();
    }


    public List<Partition> getPartitions(String topic) {
        if (!AdminUtils.topicExists(getZkUtils(), topic)) {
            return Collections.emptyList();
        }
        MetadataResponse.TopicMetadata topicMetadata = AdminUtils.fetchTopicMetadataFromZk(topic, getZkUtils());
        return topicMetadata.partitionMetadata().stream().map(p -> convertToPartition(topic, p)).collect(Collectors.toList());
    }

    public Optional<Partition> getPartition(String topic, int partition) {
        if (!AdminUtils.topicExists(getZkUtils(), topic)) {
            return Optional.empty();
        }
        MetadataResponse.TopicMetadata topicMetadata = AdminUtils.fetchTopicMetadataFromZk(topic, getZkUtils());
        return topicMetadata.partitionMetadata().stream().filter(p -> p.partition() == partition).findFirst().map(p -> convertToPartition(topic, p));
    }


    private static Partition convertToPartition(String topic,MetadataResponse.PartitionMetadata partitionMetadata) {
        Partition partition = new Partition(topic, partitionMetadata.partition());
        final Set<Integer> inSyncBrokers = partitionMetadata.isr().stream().map(Node::id).collect(Collectors.toSet());
        int leaderBroker = partitionMetadata.leader().id();
        List<Replica> replicas = partitionMetadata.replicas().stream().map(r -> convertToReplica(r, inSyncBrokers, leaderBroker)).collect(Collectors.toList());
        partition.setReplicas(replicas);
        return partition;
    }

    private static Replica convertToReplica(Node replica, Set<Integer> inSyncBrokers, int leaderBroker) {
        int broker = replica.id();
        return new Replica(broker, leaderBroker == broker, inSyncBrokers.contains(broker));
    }

}
