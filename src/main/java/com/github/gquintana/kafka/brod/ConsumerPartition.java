package com.github.gquintana.kafka.brod;

public class ConsumerPartition {
    private String topicName;
    private int id;
    private Long commitedOffset;
    private Long currentOffset;
    private String metadata;

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

    public long getCommitedOffset() {
        return commitedOffset;
    }

    public void setCommitedOffset(Long commitedOffset) {
        this.commitedOffset = commitedOffset;
    }

    public Long getCurrentOffset() {
        return currentOffset;
    }

    public void setCurrentOffset(Long currentOffset) {
        this.currentOffset = currentOffset;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Long getLag() {
        if (commitedOffset == null || currentOffset == null) {
            return null;
        }
        return currentOffset - commitedOffset;
    }
}
