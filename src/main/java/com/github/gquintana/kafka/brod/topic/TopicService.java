package com.github.gquintana.kafka.brod.topic;

import java.util.List;
import java.util.Optional;

public interface TopicService {
    void createTopic(Topic topic);

    void deleteTopic(String name);

    void updateTopic(Topic topic);

    Optional<Topic> getTopic(String name);

    boolean existTopic(String name);

    List<String> getTopics();
}
