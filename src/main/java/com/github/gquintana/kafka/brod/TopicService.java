package com.github.gquintana.kafka.brod;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZkUtils;
import org.apache.kafka.common.requests.MetadataResponse;

import java.util.List;
import java.util.Properties;

public class TopicService {
    private final ZookeeperService zookeeperService;

    public TopicService(ZookeeperService zookeeperService) {
        this.zookeeperService = zookeeperService;
    }

    private ZkUtils getZkUtils() {
        return zookeeperService.getZkUtils();
    }

    public void createTopic(Topic topic) {
        AdminUtils.createTopic(getZkUtils(), topic.getName(), topic.getPartitions(), topic.getReplicationFactor(), topic.getConfig(), RackAwareMode.Enforced$.MODULE$);
    }

    public void deleteTopic(String name) {
        AdminUtils.deleteTopic(getZkUtils(), name);
    }

    public void updateTopic(Topic topic) {
        AdminUtils.changeTopicConfig(getZkUtils(), topic.getName(), topic.getConfig());
    }

    public Topic getTopic(String name) {
        if (!AdminUtils.topicExists(getZkUtils(), name)) {
            return null;
        }
        Properties config = AdminUtils.fetchEntityConfig(getZkUtils(), "topic", name);
        MetadataResponse.TopicMetadata topicMetadata = AdminUtils.fetchTopicMetadataFromZk(name, getZkUtils());
        int partitions = topicMetadata.partitionMetadata().stream().mapToInt(MetadataResponse.PartitionMetadata::partition).max().orElse(-1) + 1;
        int replicationFactor = topicMetadata.partitionMetadata().stream().mapToInt(p -> p.replicas().size()).max().orElse(0);
        return new Topic(name, partitions, replicationFactor, config);
    }

    public List<String> getTopics() {
        return zookeeperService.getChildren("/brokers/topics");
    }
}
