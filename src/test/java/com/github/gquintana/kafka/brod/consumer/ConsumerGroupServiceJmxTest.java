package com.github.gquintana.kafka.brod.consumer;

import com.github.gquintana.kafka.brod.EmbeddedKafkaRule;
import com.github.gquintana.kafka.brod.broker.BrokerService;
import com.github.gquintana.kafka.brod.jmx.JmxConfiguration;
import com.github.gquintana.kafka.brod.jmx.JmxConnection;
import com.github.gquintana.kafka.brod.jmx.JmxException;
import com.github.gquintana.kafka.brod.jmx.JmxService;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerGroupServiceJmxTest {
    @ClassRule
    public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

    @ClassRule
    public static final EmbeddedKafkaRule KAFKA_RULE = new EmbeddedKafkaRule(TEMPORARY_FOLDER, 1);

    @Mock
    private ConsumerGroupService consumerGroupServiceMock;
    private static final String TOPIC_NAME = "consumer_jmx_service_topic";
    private static final String GROUP_ID = "consumer_jmx_service_group";
    private static final String CLIENT_ID = "consumer_jmx_service_client";

    private ConsumerGroupServiceJmx consumerGroupServiceJmx;
    private org.apache.kafka.clients.consumer.Consumer<Long, String> consumer;

    @BeforeClass
    public static void setUpClass() throws ExecutionException, InterruptedException {
        KAFKA_RULE.getKafka().createTopic(TOPIC_NAME, 1, 1);
    }

    @Before
    public void setUp() throws Exception {
        JmxService jmxService = new JmxService() {
            @Override
            public JmxConnection connect(String host, int port, JmxConfiguration configuration) throws JmxException {
                return connectLocally();
            }
        };
        consumer = KAFKA_RULE.getKafka().createConsumer(GROUP_ID, CLIENT_ID);
        consumer.subscribe(asList(TOPIC_NAME));
        Map<String, JmxConfiguration> jmxConfigurations = new HashMap<>();
        jmxConfigurations.put(GROUP_ID, new JmxConfiguration(false, 8888, null, null, 3000));
        consumerGroupServiceJmx = new ConsumerGroupServiceJmx(consumerGroupServiceMock, jmxService, jmxConfigurations);
    }

    @Test
    public void testEnrich() throws Exception {
        // Given
        String consumerId = CLIENT_ID+"-123456";
        Consumer consumer = new Consumer();
        consumer.setClientHost("/127.0.0.1");
        consumer.setClientId(CLIENT_ID);
        consumer.setId(consumerId);
        when(consumerGroupServiceMock.getConsumer(eq(GROUP_ID), eq(consumerId), isNull(String.class)))
            .thenReturn(Optional.of(consumer));
        // When
        consumer = consumerGroupServiceJmx.getConsumer(GROUP_ID, consumerId, null).get();
        // Then
        assertThat(consumer.getJmxMetrics(), not(nullValue()));
        assertThat(consumer.getJmxMetrics().entrySet(), not(empty()));
        List<String> kafkaKeys = consumer.getJmxMetrics().keySet().stream()
            .filter(k -> k.startsWith("kafka_consumer."))
            .collect(toList());
        assertThat(kafkaKeys, hasSize(9));
    }

    @After
    public void tearDown() {
        consumer.close();
        consumer = null;
    }
}
