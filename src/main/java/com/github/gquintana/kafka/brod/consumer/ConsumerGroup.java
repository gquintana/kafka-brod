package com.github.gquintana.kafka.brod.consumer;

import lombok.Data;

import java.util.List;

@Data
public class ConsumerGroup {
    private final String groupId;
    private String protocol;
    private String state;
    private List<Consumer> members;
    private String assignmentStrategy;
}
