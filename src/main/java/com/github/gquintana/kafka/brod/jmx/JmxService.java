package com.github.gquintana.kafka.brod.jmx;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JmxService {
    private final String user;
    private final String password;

    public JmxService(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public JmxConnection connect(String host, int port) throws JmxException {
        try {
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
            Map<String, Object> environment = new HashMap<>();
            if (user != null && password != null) {
                environment.put(JMXConnector.CREDENTIALS, new String[]{user, password});
            }
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, environment);
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            return new JmxConnection(jmxConnector, mBeanServerConnection);
        } catch (IOException e) {
            throw new JmxException("JMX connection to " + host + ":" + port + " failed", e);
        }
    }
}
