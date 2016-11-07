package com.github.gquintana.kafka.brod.consumer;

import java.util.List;

public class Consumer {
    private String clientId;
    private String clientHost;
    private String memberId;
    private List<ConsumerPartition> partitions;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientHost() {
        return clientHost;
    }

    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public List<ConsumerPartition> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<ConsumerPartition> partitions) {
        this.partitions = partitions;
    }
}
