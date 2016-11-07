package com.github.gquintana.kafka.brod.topic;

public class Replica {
    private int brokerId;
    private boolean leader;
    private boolean inSync;

    public Replica() {
    }

    public Replica(int brokerId, boolean leader, boolean inSync) {
        this.brokerId = brokerId;
        this.leader = leader;
        this.inSync = inSync;
    }

    public int getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(int brokerId) {
        this.brokerId = brokerId;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public boolean isInSync() {
        return inSync;
    }

    public void setInSync(boolean inSync) {
        this.inSync = inSync;
    }
}
