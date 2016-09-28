package com.github.gquintana.kafka.brod;

import java.util.ArrayList;
import java.util.List;

public class Partition {
    private String topicName;
    private int id;
    private List<Replica> replicas;

    public Partition() {
    }

    public Partition(String topicName, int id) {
        this.topicName = topicName;
        this.id = id;
        this.replicas = new ArrayList<>();
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

    public List<Replica> getReplicas() {
        return replicas;
    }

    public void setReplicas(List<Replica> replicas) {
        this.replicas = replicas;
    }
}
