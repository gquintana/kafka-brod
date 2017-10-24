package com.github.gquintana.kafka.brod.consumer;

import lombok.Data;

import java.util.List;

@Data
public class Consumer {
    private String clientId;
    private String clientHost;
    private String memberId;
    private List<ConsumerPartition> partitions;
}
