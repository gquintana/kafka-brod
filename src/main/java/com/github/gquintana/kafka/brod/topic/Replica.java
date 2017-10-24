package com.github.gquintana.kafka.brod.topic;

import lombok.Data;

@Data
public class Replica {
    private final int brokerId;
    private final boolean leader;
    private final boolean inSync;
}
