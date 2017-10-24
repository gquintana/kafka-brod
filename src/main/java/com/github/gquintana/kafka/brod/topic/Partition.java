package com.github.gquintana.kafka.brod.topic;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Partition {
    private final String topicName;
    private final int id;
    private Long beginningOffset;
    private Long endOffset;
    private List<Replica> replicas;

    public Long getRecords() {
        if (beginningOffset == null || endOffset == null) {
            return null;
        }
        return endOffset - beginningOffset;
    }
}
