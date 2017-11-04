package com.github.gquintana.kafka.brod.consumer;

import com.github.gquintana.kafka.brod.EmbeddedKafkaRule;
import com.github.gquintana.kafka.brod.jmx.JmxConfiguration;
import com.github.gquintana.kafka.brod.jmx.JmxConnection;
import com.github.gquintana.kafka.brod.jmx.JmxException;
import com.github.gquintana.kafka.brod.jmx.JmxService;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ConsumerJmxServiceTest {
    @ClassRule
    public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

    @ClassRule
    public static final EmbeddedKafkaRule KAFKA_RULE = new EmbeddedKafkaRule(TEMPORARY_FOLDER, 1);
    private static final String TOPIC_NAME = "consumer_jmx_service_topic";
    private static final String GROUP_ID = "consumer_jmx_service_group";
    private static final String CLIENT_ID = "consumer_jmx_service_client";

    private ConsumerJmxService consumerJmxService;
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
        jmxConfigurations.put(GROUP_ID, new JmxConfiguration(false, 8888, null, null));
        consumerJmxService = new ConsumerJmxService(jmxService, jmxConfigurations);
    }

    @Test
    public void testEnrich() throws Exception {
        // Given
        Consumer consumer = new Consumer();
        consumer.setClientHost("/127.0.0.1");
        consumer.setClientId(CLIENT_ID);
        // When
        consumer = consumerJmxService.enrich(GROUP_ID, consumer);
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
