package com.github.gquintana.kafka.brod.consumer;

import java.util.List;
import java.util.Optional;

public interface ConsumerGroupService {
    List<String> getGroupIds();

    Optional<ConsumerGroup> getGroup(String groupId);

    Optional<ConsumerGroup> getGroup(String groupId, String topic);

    Optional<Consumer> getConsumer(String groupId, String consumerId, String topic);
}
