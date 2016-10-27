package com.github.gquintana.kafka.brod;

import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

import java.util.List;

public class ZookeeperService implements AutoCloseable {
    private final String connectString;
    private final int sessionTimeout;
    private final int connectionTimeout;

    private Connection connection;

    public ZookeeperService(String connectString, int sessionTimeout, int connectionTimeout) {
        this.connectString = connectString;
        this.sessionTimeout = sessionTimeout;
        this.connectionTimeout = connectionTimeout;
    }

    private static class Connection implements AutoCloseable {
        private final ZkClient zkClient;
        private final ZkConnection zkConnection;
        private final ZkUtils zkUtils;
        private Connection(String connectString, int sessionTimeout, int connectionTimeout) {
            zkClient = new ZkClient(connectString, sessionTimeout, connectionTimeout, ZKStringSerializer$.MODULE$);
            zkConnection = new ZkConnection(connectString, sessionTimeout);
            zkUtils = new ZkUtils(zkClient, zkConnection, false);
        }

        @Override
        public void close() throws Exception {
            zkUtils.close();
        }
    }

    private Connection connection() {
        if (connection == null) {
            connection = new Connection(connectString, sessionTimeout, connectionTimeout);
        }
        return connection;
    }

    public List<String> getChildren(String path) {
        try {
            return getZkClient().getChildren(path);
        } catch (RuntimeException e) {
            close();
            throw e;
        }
    }

    public <T> T getData(String path) {
        try {
            return getZkClient().readData(path);
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    public ZkUtils getZkUtils() {
        return connection().zkUtils;
    }

    public ZkClient getZkClient() {
        return connection().zkClient;
    }

    @Override
    public void close() {
        Connection lConnection = this.connection;
        if (lConnection != null) {
            try {
                lConnection.close();
            } catch (Exception e) {

            }
            this.connection = null;
        }
    }
}
