package com.github.gquintana.kafka.brod.jmx;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

public class JmxServiceTest {

    private static Process jmxAppProcess;
    private static File jmxAppLogFile;
    private JmxService jmxService = new JmxService(null, null);

    private static String toPath(URL url) {
        try {
            return new File(url.toURI()).getPath();
        } catch(URISyntaxException e) {
            return url.getPath();
        }
    }
    private static String getClasspath() {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader) cl).getURLs();
        return Stream.of(urls).map(JmxServiceTest::toPath).collect(joining(File.pathSeparator));
    }
    @BeforeClass
    public static void setUpClass() throws IOException {
        File targetDir = new File("target");
        String[] cmd = {"java",
            "-classpath", getClasspath(),
            "-Dcom.sun.management.jmxremote=true",
            "-Dcom.sun.management.jmxremote.port=4321",
            "-Dcom.sun.management.jmxremote.authenticate=false",
            "-Dcom.sun.management.jmxremote.ssl=false",
            JmxApp.class.getName()
        };
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.directory(targetDir);
        File logFile = new File(targetDir, "JmxApp.log");
        processBuilder.redirectOutput(logFile);
        processBuilder.redirectError(logFile);
        processBuilder.inheritIO();
        jmxAppProcess = processBuilder.start();
        jmxAppLogFile = logFile;
    }

    @Test
    public void testConnect() {
        // When
        try(JmxConnection jmxConnection = jmxService.connect("localhost", 4321)) {
            assertNotNull(jmxConnection);
            assertThat(jmxConnection.getId(), not(isEmptyOrNullString()));
        }
    }

    @Test
    public void testGetAttributes() {
        // When
        try(JmxConnection jmxConnection = jmxService.connect("localhost", 4321)) {
            Map<String, Object> attributes = jmxConnection.getAttributes("java.lang:type=OperatingSystem", "SystemLoadAverage", "OpenFileDescriptorCount");
            assertNotNull(attributes.get("SystemLoadAverage"));
            assertNotNull(attributes.get("OpenFileDescriptorCount"));
        }
    }

    @AfterClass
    public static void tearDownClass() {
        jmxAppProcess.destroy();
        jmxAppLogFile.delete();
    }
}
