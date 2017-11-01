package com.github.gquintana.kafka.brod;

import com.github.gquintana.kafka.brod.security.FileBasedUserService;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

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

    @Test
    public void testLoadAndGetBoolean() throws Exception {
        // Given
        Configuration configuration = new Configuration();
        // When
        configuration.load("brod.properties");
        boolean httpJsonPretty = configuration.getAsBoolean("http.json.pretty").get();
        // Then
        assertThat(httpJsonPretty, is(true));
    }

    @Test
    public void testLoadAndGetConfiguration() throws Exception {
        // Given
        Configuration configuration = new Configuration();
        // When
        configuration.load("brod.properties");
        Configuration securityConfig = configuration.getAsConfiguration("http.security");
        // Then
        assertThat(securityConfig, notNullValue());
        assertThat(securityConfig.getAsBoolean("enabled").get(), notNullValue());
    }

    @Test
    public void testLoadAndClass() throws Exception {
        // Given
        Configuration configuration = new Configuration();
        // When
        configuration.load("brod.properties");
        Class securityServiceClass = configuration.getAsClass("http.security.service.class").get();
        // Then
        assertThat(securityServiceClass, equalTo(FileBasedUserService.class));
    }
}
