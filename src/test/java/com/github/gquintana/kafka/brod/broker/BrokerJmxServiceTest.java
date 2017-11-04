package com.github.gquintana.kafka.brod.broker;

import com.github.gquintana.kafka.brod.EmbeddedKafkaRule;
import com.github.gquintana.kafka.brod.jmx.JmxConfiguration;
import com.github.gquintana.kafka.brod.jmx.JmxConnection;
import com.github.gquintana.kafka.brod.jmx.JmxException;
import com.github.gquintana.kafka.brod.jmx.JmxService;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class BrokerJmxServiceTest {
    @ClassRule
    public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

    @ClassRule
    public static final EmbeddedKafkaRule KAFKA_RULE = new EmbeddedKafkaRule(TEMPORARY_FOLDER, 1);

    private BrokerJmxService brokerJmxService;

    @Before
    public void setUp() throws Exception {
        JmxService jmxService = new JmxService() {
            @Override
            public JmxConnection connect(String host, int port, JmxConfiguration configuration) throws JmxException {
                return connectLocally();
            }
        };
        brokerJmxService = new BrokerJmxService(jmxService, new JmxConfiguration(false, null,null, null));
    }

    @Test
    public void testEnrich() throws Exception {
        // Given
        Broker broker = new Broker(0);
        broker.setHost("localhost");
        broker.setJmxPort(9999);
        // When
        broker = brokerJmxService.enrich(broker);
        // Then
        assertThat(broker.getJmxMetrics(), not(nullValue()));
        assertThat(broker.getJmxMetrics().entrySet(), not(empty()));
        assertThat(broker.getJmxMetrics().containsKey("kafka_server.broker_topic_metrics.bytes_in_per_sec.five_minute_rate"), is(true));
        assertThat(broker.getJmxMetrics().containsKey("kafka_server.broker_topic_metrics.bytes_out_per_sec.one_minute_rate"), is(true));
        assertThat(broker.getJmxMetrics().containsKey("kafka_server.broker_topic_metrics.messages_in_per_sec.five_minute_rate"), is(true));
        assertThat(broker.getJmxMetrics().containsKey("java_lang.memory.heap_memory_usage.used"), is(true));
    }
}
