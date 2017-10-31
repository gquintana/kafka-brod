package com.github.gquintana.kafka.brod.topic;

import com.github.gquintana.kafka.brod.EmbeddedKafkaRule;
import com.github.gquintana.kafka.brod.broker.Broker;
import com.github.gquintana.kafka.brod.jmx.JmxConfiguration;
import com.github.gquintana.kafka.brod.jmx.JmxConnection;
import com.github.gquintana.kafka.brod.jmx.JmxException;
import com.github.gquintana.kafka.brod.jmx.JmxService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PartitionJmxServiceTest {
    @ClassRule
    public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

    @ClassRule
    public static final EmbeddedKafkaRule KAFKA_RULE = new EmbeddedKafkaRule(TEMPORARY_FOLDER, 1);
    public static final String TOPIC_NAME = "partition_jmx_test";
    public static final int TOPIC_PARTITIONS = 3;

    private PartitionJmxService partitionJmxService;

    @Mock
    private Supplier<Broker> brokerSupplierMock;

    @BeforeClass
    public static void setUpClass() throws Exception {
        KAFKA_RULE.getKafka().createTopic(TOPIC_NAME, TOPIC_PARTITIONS, 1);
        KAFKA_RULE.getKafka().send(TOPIC_NAME, "partition_jmx_test");
    }

    @Before
    public void setUp() throws Exception {
        JmxService jmxService = new JmxService() {
            @Override
            public JmxConnection connect(String host, int port, JmxConfiguration configuration) throws JmxException {
                return connectLocally();
            }
        };
        partitionJmxService = new PartitionJmxService(jmxService, brokerSupplierMock, new JmxConfiguration(false, null, null));
    }

    @Test
    public void testEnrich() throws Exception {
        // Given
        Broker broker = new Broker(0);
        broker.setHost("localhost");
        broker.setJmxPort(9999);
        when(brokerSupplierMock.get()).thenReturn(broker);
        List<Partition> partitions = IntStream.range(0, TOPIC_PARTITIONS)
            .mapToObj(i -> new Partition(TOPIC_NAME, i)).collect(toList());
        // When
        partitions = partitionJmxService.enrich(partitions);
        // Then
        assertThat(partitions, not(nullValue()));
        assertThat(partitions, not(empty()));
        Partition partition = partitions.get(0);
        assertThat(partition.getNumSegments(), not(nullValue()));
        assertThat(partition.getNumSegments(), greaterThan(0L));
        assertThat(partition.getSize(), not(nullValue()));
    }
}
