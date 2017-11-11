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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BrokerServiceJmxTest {
    @ClassRule
    public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

    @ClassRule
    public static final EmbeddedKafkaRule KAFKA_RULE = new EmbeddedKafkaRule(TEMPORARY_FOLDER, 1);

    @Mock
    private BrokerService brokerServiceMock;
    private BrokerServiceJmx brokerServiceJmx;

    @Before
    public void setUp() throws Exception {
        JmxService jmxService = new JmxService() {
            @Override
            public JmxConnection connect(String host, int port, JmxConfiguration configuration) throws JmxException {
                return connectLocally();
            }
        };
        brokerServiceJmx = new BrokerServiceJmx(brokerServiceMock, jmxService, new JmxConfiguration(false, null,null, null, 3000));
    }

    @Test
    public void testEnrich() throws Exception {
        // Given
        Broker broker = new Broker(0);
        broker.setHost("localhost");
        broker.setJmxPort(9999);
        when(brokerServiceMock.getBroker(eq(0))).thenReturn(Optional.of(broker));
        // When
        broker = brokerServiceJmx.getBroker(0).get();
        // Then
        assertThat(broker.getJmxMetrics(), not(nullValue()));
        assertThat(broker.getJmxMetrics().entrySet(), not(empty()));
        assertThat(broker.getJmxMetrics().containsKey("kafka_server.broker_topic_metrics.bytes_in_per_sec.five_minute_rate"), is(true));
        assertThat(broker.getJmxMetrics().containsKey("kafka_server.broker_topic_metrics.bytes_out_per_sec.one_minute_rate"), is(true));
        assertThat(broker.getJmxMetrics().containsKey("kafka_server.broker_topic_metrics.messages_in_per_sec.five_minute_rate"), is(true));
        assertThat(broker.getJmxMetrics().containsKey("java_lang.memory.heap_memory_usage.used"), is(true));
    }
}
