package com.github.gquintana.kafka.brod;

import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.util.List;

public class ZookeeperService implements AutoCloseable {
    private final String connectString;
    private final int sessionTimeout;
    private final int connectionTimeout;
    private ZkClient zkClient;
    private ZkConnection zkConnection;
    private ZkUtils zkUtils;

    public ZookeeperService(String connectString, int sessionTimeout, int connectionTimeout) {
        this.connectString = connectString;
        this.sessionTimeout = sessionTimeout;
        this.connectionTimeout = connectionTimeout;
    }

    public void connect() {
        zkClient = new ZkClient(connectString, sessionTimeout, connectionTimeout, ZKStringSerializer$.MODULE$);
        zkConnection = new ZkConnection(connectString, sessionTimeout);
        zkUtils = new ZkUtils(zkClient, zkConnection, false);
    }

    public List<String> getChildren(String path) {
        return zkClient.getChildren(path);
    }

    public <T> T getData(String path) {
        return zkClient.readData(path);
    }

    public ZkUtils getZkUtils() {
        return zkUtils;
    }

    @Override
    public void close() throws Exception {
        zkUtils.close();
    }
}
