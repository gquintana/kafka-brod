package com.github.gquintana.kafka.brod.jmx;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class JmxApp {
    private static boolean running;

    private static void log(String message) {
        System.out.println(String.format("%1$tH:%1$tM:%1$tS %2$s", new Date(), message));
    }

    public static void main(String[] args) throws InterruptedException {
        log(JmxApp.class.getSimpleName() + " started");
        running = true;
        long l = 0;
        while (running) {
            if (l % 10 == 0) {
                log(JmxApp.class.getSimpleName() + " is alive");
            }
            Thread.sleep(1000L);
            l++;
        }
    }

    private static String toPath(URL url) {
        try {
            return new File(url.toURI()).getPath();
        } catch (URISyntaxException e) {
            return url.getPath();
        }
    }

    private static String getClasspath() {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader) cl).getURLs();
        return Stream.of(urls).map(JmxApp::toPath).collect(joining(File.pathSeparator));
    }

    public static Process startProcess(File targetDir, int jmxPort) throws IOException, InterruptedException {
        String[] cmd = {"java",
            "-classpath", getClasspath(),
            "-Dcom.sun.management.jmxremote=true",
            "-Dcom.sun.management.jmxremote.port=" + jmxPort,
            "-Dcom.sun.management.jmxremote.authenticate=false",
            "-Dcom.sun.management.jmxremote.ssl=false",
            JmxApp.class.getName()
        };
        // Start process
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.directory(targetDir);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = processBuilder.start();
        // Wait for process to be listening on JMX port
        long start = System.currentTimeMillis();
        boolean success = false;
        while (!success && (System.currentTimeMillis() - start) < 5000L) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("localhost", jmxPort));
                success = true;
            } catch (IOException e) {
                Thread.sleep(200L);
            }
        }
        if (!success) {
            throw new IOException("Failed to start process");
        }
        return process;
    }

    private static final int BASE_JMX_PORT = 9876;

    public static int findAvailablePort() throws IOException {
        IOException lastExc = null;
        for (int port = BASE_JMX_PORT; port < BASE_JMX_PORT + 100; port++) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                return port;
            } catch (IOException e) {
                // Port not available
                lastExc = e;
            }
        }
        throw lastExc;
    }
}
