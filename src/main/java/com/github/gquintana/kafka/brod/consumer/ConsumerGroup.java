package com.github.gquintana.kafka.brod.consumer;

import lombok.Data;

import java.util.List;

@Data
public class ConsumerGroup {
    private final String id;
    private String protocol;
    private String state;
    private List<Consumer> members;
    private String assignmentStrategy;
}
