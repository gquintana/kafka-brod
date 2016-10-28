package com.github.gquintana.kafka.brod;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ConfigurationTest {

    @Test
    public void testLoadAndGetString() throws Exception {
        // Given
        Configuration configuration = new Configuration();
        // When
        configuration.load("brod.properties");
        String zookeeperServers = configuration.getAsString("zookeeper.servers").get();
        // Then
        assertThat("localhost:2181", equalTo(zookeeperServers));
    }

    @Test
    public void testLoadAndGetInt() throws Exception {
        // Given
        Configuration configuration = new Configuration();
        // When
        configuration.load("brod.properties");
        int zookeeperServers = configuration.getAsInteger("zookeeper.sessionTimeout").get();
        // Then
        assertThat(3000, equalTo(zookeeperServers));
    }
}
