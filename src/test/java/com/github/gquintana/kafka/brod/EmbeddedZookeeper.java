package com.github.gquintana.kafka.brod;

import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EmbeddedZookeeper {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedZookeeper.class);
    private ZooKeeperServerMain server;
    private ServerCnxnFactory serverCnxnFactory;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final File dataDir;

    public EmbeddedZookeeper(File dataDir) {
        this.dataDir = dataDir;
    }

    public static EmbeddedZookeeper createAndStart(TemporaryFolder temporaryFolder) throws IOException {
        File zookeeperData = temporaryFolder.newFolder("zookeeper");
        EmbeddedZookeeper zookeeper = new EmbeddedZookeeper(zookeeperData);
        zookeeper.start();
        return zookeeper;
    }

    public void start() throws IOException {
        LOGGER.info("Starting Zookeeper");
        Properties properties = TestResources.getResourceAsProperties("/zookeeper.properties");
        properties.setProperty("dataDir", dataDir.getAbsolutePath());
        QuorumPeerConfig quorumConfiguration = new QuorumPeerConfig();
        try {
            quorumConfiguration.parseProperties(properties);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        final ServerConfig configuration = new ServerConfig();
        configuration.readFrom(quorumConfiguration);
        executor.execute(() -> runServer(configuration));
        long timeout = System.currentTimeMillis() + 1000L;
        try {
            while(getServerCnxnFactory() == null && System.currentTimeMillis() < timeout) {
                Thread.sleep(100L);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Get hidden ServerCnxnFactory field through reflection */
    private ServerCnxnFactory getServerCnxnFactory() {
        if (serverCnxnFactory != null) {
            return serverCnxnFactory;
        }
        if (server == null) {
            return null;
        }
        try {
            serverCnxnFactory = getField(server, "cnxnFactory", ServerCnxnFactory.class);
            return serverCnxnFactory;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get field value using reflection
     */
    private static <T> T getField(Object target, String fieldName, Class<T> fieldClass) throws NoSuchFieldException, IllegalAccessException {
        Class<?> targetClass = target.getClass();
        Field field = targetClass.getDeclaredField(fieldName);
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        Object fieldValue = field.get(target);
        return fieldClass.cast(fieldValue);
    }

    public void stop() {
        LOGGER.info("Stopping Zookeeper");
        ServerCnxnFactory serverCnxFactory = getServerCnxnFactory();
        if (serverCnxFactory != null) {
            serverCnxFactory.closeAll();
            serverCnxFactory.shutdown();
        }
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.serverCnxnFactory = null;
        this.server = null;
    }

    private void runServer(ServerConfig configuration) {
        try {
            server = new ZooKeeperServerMain();
            server.runFromConfig(configuration);
        } catch (IOException e) {
            LOGGER.error("ZooKeeper Failed", e);
        }
    }
}
