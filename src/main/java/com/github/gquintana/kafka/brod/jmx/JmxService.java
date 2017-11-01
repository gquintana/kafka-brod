package com.github.gquintana.kafka.brod.jmx;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.naming.Context;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

public class JmxService {
    public JmxConnection connect(String host, int port, JmxConfiguration configuration) throws JmxException {
        try {
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
            Map<String, Object> environment = new HashMap<>();
            if (configuration != null) {
                if (configuration.isAuthentication()) {
                    environment.put(JMXConnector.CREDENTIALS, new String[]{configuration.getUser(), configuration.getPassword()});
                }
                if (configuration.isSsl()) {
                    environment.put(Context.SECURITY_PROTOCOL, "ssl");
                    SslRMIClientSocketFactory socketFactory = new SslRMIClientSocketFactory();
                    environment.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, socketFactory);
                    // com.sun.jndi.rmi.registry.RegistryContext.SOCKET_FACTORY
                    environment.put("com.sun.jndi.rmi.factory.socket", socketFactory);
                }
            }
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, environment);
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            return new JmxConnection(jmxConnector, mBeanServerConnection);
        } catch (IOException|SecurityException e) {
            throw new JmxException("JMX connection to " + host + ":" + port + " failed", e);
        }
    }

    public JmxConnection connectLocally() {
        return new JmxConnection(null, ManagementFactory.getPlatformMBeanServer());
    }
}
