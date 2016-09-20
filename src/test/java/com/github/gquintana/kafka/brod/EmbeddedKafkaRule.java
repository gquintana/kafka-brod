package com.github.gquintana.kafka.brod;

import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import java.io.File;

public class EmbeddedKafkaRule extends ExternalResource {
    private final TemporaryFolder temporaryFolder;

    private EmbeddedZookeeper zookeeper;
    private EmbeddedKafka kafka;

    public EmbeddedKafkaRule(TemporaryFolder temporaryFolder) {
        this.temporaryFolder = temporaryFolder;
    }

    public EmbeddedZookeeper getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(EmbeddedZookeeper zookeeper) {
        this.zookeeper = zookeeper;
    }

    public EmbeddedKafka getKafka() {
        return kafka;
    }

    public void setKafka(EmbeddedKafka kafka) {
        this.kafka = kafka;
    }

    @Override
    protected void before() throws Throwable {
        File zookeeperData = temporaryFolder.newFolder("zookeeper");
        zookeeper = new EmbeddedZookeeper(zookeeperData);
        zookeeper.start();
        File kafkaData = temporaryFolder.newFolder("kafka");
        kafka = new EmbeddedKafka(kafkaData);
        kafka.start();
    }

    @Override
    protected void after() {
        kafka.stop();
        zookeeper.stop();
    }
}
