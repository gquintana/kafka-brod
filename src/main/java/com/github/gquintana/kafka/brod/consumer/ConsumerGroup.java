package com.github.gquintana.kafka.brod.consumer;

import java.util.List;

public class ConsumerGroup {
    private String groupId;
    private String protocol;
    private String state;
    private List<Consumer> members;
    private String assignmentStrategy;


    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<Consumer> getMembers() {
        return members;
    }

    public void setMembers(List<Consumer> members) {
        this.members = members;
    }

    public String getAssignmentStrategy() {
        return assignmentStrategy;
    }

    public void setAssignmentStrategy(String assignmentStrategy) {
        this.assignmentStrategy = assignmentStrategy;
    }
}
