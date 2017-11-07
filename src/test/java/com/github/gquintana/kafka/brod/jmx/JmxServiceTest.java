package com.github.gquintana.kafka.brod.jmx;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class JmxServiceTest {

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
    public void testConnect() {
        // When
        try(JmxConnection jmxConnection = jmxService.connect("localhost", jmxAppPort, null)) {
            assertNotNull(jmxConnection);
            assertThat(jmxConnection.getId(), not(isEmptyOrNullString()));
        }
    }

    @Test(expected = JmxException.class)
    public void testConnectNotFound() {
        JmxConnection jmxConnection = jmxService.connect("localhost", 12345, null);
    }

    @Test
    public void testGetAttributes() {
        // When
        try(JmxConnection jmxConnection = jmxService.connect("localhost", jmxAppPort, null)) {
            Map<String, Object> attributes = jmxConnection.getAttributes("java.lang:type=OperatingSystem", "SystemLoadAverage", "OpenFileDescriptorCount");
            assertNotNull(attributes.get("java_lang.operating_system.system_load_average"));
            assertNotNull(attributes.get("java_lang.operating_system.open_file_descriptor_count"));
        }
    }

    @Test
    public void testGetCompositeAttributes() {
        // When
        try(JmxConnection jmxConnection = jmxService.connect("localhost", jmxAppPort, null)) {
            Map<String, Object> attributes = jmxConnection.getAttributes("java.lang:type=Memory", "HeapMemoryUsage", "NonHeapMemoryUsage");
            assertNotNull(attributes.get("java_lang.memory.non_heap_memory_usage.used"));
            assertNotNull(attributes.get("java_lang.memory.heap_memory_usage.committed"));
            assertThat(attributes.entrySet(), hasSize(8));
        }
    }



    @AfterClass
    public static void tearDownClass() {
        jmxAppProcess.destroy();
    }
}
