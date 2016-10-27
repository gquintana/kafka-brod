package com.github.gquintana.kafka.brod;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class ZookeeperServiceTest {
    @ClassRule
    public static final TemporaryFolder temporaryFolder = new TemporaryFolder();
    private static EmbeddedZookeeper zookeeper;
    private static EmbeddedKafka kafka;
    private static ZookeeperService zookeeperService;


    @BeforeClass
    public static void setUpClass() throws IOException {
        File zookeeperData = temporaryFolder.newFolder("zookeeper");
        zookeeper = new EmbeddedZookeeper(zookeeperData);
        zookeeper.start();
        File kafkaData = temporaryFolder.newFolder("kafka");
        kafka = new EmbeddedKafka(kafkaData);
        kafka.start();
        zookeeperService = new ZookeeperService("localhost:2181", 3000, 3000);
    }

    @Test
    public void testGetChildren() throws IOException {
        // When
        List<String> brokers = zookeeperService.getChildren("/brokers/ids");
        // Then
        assertThat(brokers.size(), equalTo(1));
        assertThat(brokers.get(0), equalTo("0"));
    }

    @Test
    public void testGetChildrenRoot() throws IOException {
        // When
        List<String> roots = zookeeperService.getChildren("/");
        // Then
        assertThat(roots, hasItem("brokers"));
    }

    @Test
    public void testData() throws IOException {
        // When
        String broker0 = zookeeperService.getData("/brokers/ids/0");
        // Then
        assertThat(broker0, notNullValue());
        assertThat(broker0, containsString("\"endpoints\":"));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        zookeeperService.close();
        kafka.stop();
        zookeeper.stop();
    }
}
