package com.github.gquintana.kafka.brod;

public class ConsumerPartition {
    private String topicName;
    private int id;
    private Long topicOffset;
    private Long consumerOffset;
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

    public long getTopicOffset() {
        return topicOffset;
    }

    public void setTopicOffset(Long topicOffset) {
        this.topicOffset = topicOffset;
    }

    public Long getConsumerOffset() {
        return consumerOffset;
    }

    public void setConsumerOffset(Long consumerOffset) {
        this.consumerOffset = consumerOffset;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Long getLag() {
        if (topicOffset == null || consumerOffset == null) {
            return null;
        }
        return topicOffset - consumerOffset;
    }
}
