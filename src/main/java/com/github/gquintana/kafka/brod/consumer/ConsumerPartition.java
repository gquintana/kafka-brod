package com.github.gquintana.kafka.brod.consumer;

public class ConsumerPartition {
    private String topicName;
    private int id;
    private Long commitedOffset;
    private Long endOffset;

    public ConsumerPartition() {
    }

    public ConsumerPartition(String topicName, int id) {
        this.topicName = topicName;
        this.id = id;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getCommitedOffset() {
        return commitedOffset;
    }

    public void setCommitedOffset(Long commitedOffset) {
        this.commitedOffset = commitedOffset;
    }

    public Long getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(Long endOffset) {
        this.endOffset = endOffset;
    }

    public Long getLag() {
        if (commitedOffset == null || endOffset == null) {
            return null;
        }
        return endOffset - commitedOffset;
    }
}
