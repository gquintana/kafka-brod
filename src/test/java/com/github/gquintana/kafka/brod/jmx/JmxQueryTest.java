package com.github.gquintana.kafka.brod.jmx;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class JmxQueryTest {
    private static Process jmxAppProcess;
    private JmxService jmxService = new JmxService();
    private static int jmxAppPort;

    @BeforeClass
    public static void setUpClass() throws IOException, InterruptedException {
        File targetDir = new File("target");
        jmxAppPort = JmxApp.findAvailablePort();
        jmxAppProcess = JmxApp.startProcess(targetDir, jmxAppPort);
    }

    @Test
    public void testSimple() {
        // Given
        JmxQuery jmxQuery = new JmxQuery.Builder()
            .withAttributes("java.lang:type=OperatingSystem", "SystemLoadAverage", "OpenFileDescriptorCount")
            .build();
        // When
        try(JmxConnection jmxConnection = jmxService.connect("localhost", jmxAppPort, null)) {
            Map<String, Object> attributes = jmxQuery.execute(jmxConnection);
            // Then
            assertNotNull(attributes.get("java_lang.operating_system.system_load_average"));
            assertNotNull(attributes.get("java_lang.operating_system.open_file_descriptor_count"));
        }
    }

    @Test
    public void testGetCompositeAttributes() {
        // Given
        JmxQuery jmxQuery = new JmxQuery.Builder()
            .withJavaAttributes()
            .build();
        // When
        try(JmxConnection jmxConnection = jmxService.connect("localhost", jmxAppPort, null)) {
            Map<String, Object> attributes = jmxQuery.execute(jmxConnection);
            assertNotNull(attributes.get("java_lang.memory.non_heap_memory_usage.used"));
            assertNotNull(attributes.get("java_lang.memory.heap_memory_usage.committed"));
            assertNotNull(attributes.get("java_lang.operating_system.system_load_average"));
            assertNotNull(attributes.get("java_lang.operating_system.open_file_descriptor_count"));
            assertThat(attributes.entrySet(), hasSize(17));
        }
    }

    @Test
    public void testBestEffort() {
        // Given
        JmxQuery jmxQuery = new JmxQuery.Builder()
            .withAttributes("java.lang:type=OperatingSystem", "SystemLoadAverage")
            .withAttributes("java.lang:type=NotExist", "NotExist")
            .withAttributes("java.lang:type=OperatingSystem", "OpenFileDescriptorCount")
            .build();
        // When
        try(JmxConnection jmxConnection = jmxService.connect("localhost", jmxAppPort, null)) {
            Map<String, Object> attributes = jmxQuery.execute(jmxConnection);
            // Then
            assertNotNull(attributes.get("java_lang.operating_system.system_load_average"));
            assertNotNull(attributes.get("java_lang.operating_system.open_file_descriptor_count"));
        }
    }

    @AfterClass
    public static void tearDownClass() {
        jmxAppProcess.destroy();
    }

}
