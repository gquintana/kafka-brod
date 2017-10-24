package com.github.gquintana.kafka.brod.consumer;

import lombok.Data;

@Data
public class ConsumerPartition {
    private final String topicName;
    private final int id;
    private Long commitedOffset;
    private Long endOffset;

    public Long getLag() {
        if (commitedOffset == null || endOffset == null) {
            return null;
        }
        return endOffset - commitedOffset;
    }
}
