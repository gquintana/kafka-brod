package com.github.gquintana.kafka.brod;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class BrokerServiceTest {

    @ClassRule
    public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

    @ClassRule
    public static final EmbeddedKafkaRule KAFKA_RULE = new EmbeddedKafkaRule(TEMPORARY_FOLDER, 2);

    private static BrokerService brokerService;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ZookeeperService zookeeperService = new ZookeeperService("localhost:2181", 3000, 3000);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        brokerService = new BrokerService(zookeeperService, objectMapper);
    }

    @Test
    public void testGetBrokers() throws Exception {
        // When
        List<Integer> brokers = brokerService.getBrokers();
        // Then
        assertThat(brokers, Matchers.hasSize(2));
        assertThat(brokers, hasItems(0, 1));
    }

    @Test
    public void testParseBrokerPlainText() throws Exception {
        // Given
        String json = "{\"jmx_port\":-1,\"timestamp\":\"1474316866507\",\"endpoints\":[\"PLAINTEXT://kafkahost1:9092\"],\"host\":\"kafkahost1\",\"version\":3,\"port\":9092}";
        // When
        Broker broker = brokerService.parseBroker(1, json);
        // Then
        assertThat(broker.getHost(), equalTo("kafkahost1"));
        assertThat(broker.getPort(), equalTo(9092));
        assertThat(broker.getJmxPort(), nullValue());
        assertThat(broker.getEndpoints().size(), equalTo(1));
    }

    @Test
    public void testParseBrokerSslAndJmx() throws Exception {
        // Given
        String json = "{\"jmx_port\":9999,\"timestamp\":\"1474317250472\",\"endpoints\":[\"SSL://kafkahost1:9092\"],\"host\":null,\"version\":3,\"port\":-1}";
        // When
        Broker broker = brokerService.parseBroker(1, json);
        // Then
        assertThat(broker.getHost(), equalTo("kafkahost1"));
        assertThat(broker.getPort(), equalTo(9092));
        assertThat(broker.getJmxPort(), equalTo(9999));
        assertThat(broker.getEndpoints().size(), equalTo(1));
    }

    @Test
    public void testParseBrokerPlainAndSsl() throws Exception {
        // Given
        String json = "{\"jmx_port\":-1,\"timestamp\":\"1474317250472\",\"endpoints\":[\"PLAINTEXT://:9092\",\"SSL://kafkahost2:9093\"],\"host\":null,\"version\":3,\"port\":-1}";
        // When
        Broker broker = brokerService.parseBroker(1, json);
        // Then
        assertThat(broker.getHost(), equalTo("kafkahost2"));
        assertThat(broker.getPort(), nullValue());
        assertThat(broker.getJmxPort(), nullValue());
        assertThat(broker.getEndpoints().size(), equalTo(2));
    }

    @Test
    public void testGetBroker() throws Exception {
        // When
        Broker broker = brokerService.getBroker(0).get();
        // Then
        assertThat(broker, notNullValue());
        assertThat(broker.getId(), is(0));
        assertThat(broker.getPort(), equalTo(9092));
        assertThat(broker.getEndpoints().size(), equalTo(1));
        assertThat(broker.getController().booleanValue(), is(true));

    }

    @Test
    public void testController() throws Exception {
        // When
        int controllerId = brokerService.getController().get();
        // Then
        assertThat(controllerId, equalTo(0));

    }

}