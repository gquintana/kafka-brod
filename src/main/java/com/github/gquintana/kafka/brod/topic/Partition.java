package com.github.gquintana.kafka.brod.topic;

import lombok.Data;

import java.util.List;

@Data
public class Partition {
    private final String topicName;
    private final int id;
    private final Long beginningOffset;
    private final Long endOffset;
    private final List<Replica> replicas;
    private Long size;
    private Long numSegments;

    public Long getRecords() {
        if (beginningOffset == null || endOffset == null) {
            return null;
        }
        return endOffset - beginningOffset;
    }
}
