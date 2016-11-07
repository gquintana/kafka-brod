package com.github.gquintana.kafka.brod.topic;

import com.github.gquintana.kafka.brod.ZookeeperService;
import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.server.ConfigType;
import kafka.utils.ZkUtils;
import org.apache.kafka.common.requests.MetadataResponse;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

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

    public Optional<Topic> getTopic(String name) {
        if (!existTopic(name)) {
            return Optional.empty();
        }
        Properties config = AdminUtils.fetchEntityConfig(getZkUtils(), ConfigType.Topic(), name);
        MetadataResponse.TopicMetadata topicMetadata = AdminUtils.fetchTopicMetadataFromZk(name, getZkUtils());
        int partitions = topicMetadata.partitionMetadata().stream().mapToInt(MetadataResponse.PartitionMetadata::partition).max().orElse(-1) + 1;
        int replicationFactor = topicMetadata.partitionMetadata().stream().mapToInt(p -> p.replicas().size()).max().orElse(0);
        return Optional.of(new Topic(name, partitions, replicationFactor, config));
    }

    public boolean existTopic(String name) {
        return AdminUtils.topicExists(getZkUtils(), name);
    }

    public List<String> getTopics() {
        return zookeeperService.getChildren("/brokers/topics").stream().sorted().collect(Collectors.toList());
    }
}
