package com.github.gquintana.kafka.brod.jmx;

import lombok.AllArgsConstructor;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.naming.Context;
import javax.net.SocketFactory;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.util.HashMap;
import java.util.Map;

public class JmxService {

    /**
     * @see com.sun.jndi.rmi.registry.RegistryContext#SOCKET_FACTORY
     */
    private static final String RMI_REGISTRY_SOCKET_FACTORY_ATTRIBUTE = "com.sun.jndi.rmi.factory.socket";

    /*
             * The socket factory must be a subclass of {@link javax.rmi.ssl.SslRMIClientSocketFactory} when SSL is enabled
             */
    @AllArgsConstructor
    private static class SslRMIClientSocketFactory extends javax.rmi.ssl.SslRMIClientSocketFactory {
        private final int soTimeout;
        @Override
        public Socket createSocket(String host, int port) throws IOException {
            Socket socket = super.createSocket(host, port);
            socket.setSoTimeout(soTimeout);
            return socket;
        }
    }

    @AllArgsConstructor
    private static class DefaultRMIClientSocketFactory implements RMIClientSocketFactory {
        private final int soTimeout;
        @Override
        public Socket createSocket(String host, int port) throws IOException {
            Socket socket = SocketFactory.getDefault().createSocket(host, port);
            socket.setSoTimeout(soTimeout);
            return socket;
        }
    }

    public JmxConnection connect(String host, int port, JmxConfiguration configuration) throws JmxException {
        try {
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
            Map<String, Object> environment = new HashMap<>();
            RMIClientSocketFactory socketFactory;
            if (configuration == null) {
                socketFactory = new DefaultRMIClientSocketFactory(JmxConfiguration.SO_TIMEOUT_DEFAULT);
            } else {
                if (configuration.isAuthentication()) {
                    environment.put(JMXConnector.CREDENTIALS, new String[]{configuration.getUser(), configuration.getPassword()});
                }
                if (configuration.isSsl()) {
                    environment.put(Context.SECURITY_PROTOCOL, "ssl");
                    socketFactory = new SslRMIClientSocketFactory(configuration.getSoTimeout());
                } else {
                    socketFactory = new DefaultRMIClientSocketFactory(configuration.getSoTimeout());
                }
            }
            environment.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, socketFactory);
            environment.put(RMI_REGISTRY_SOCKET_FACTORY_ATTRIBUTE, socketFactory);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, environment);
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            return new JmxConnection(jmxConnector, mBeanServerConnection);
        } catch (IOException | SecurityException e) {
            throw new JmxException("JMX connection to " + host + ":" + port + " failed", e);
        }
    }

    public JmxConnection connectLocally() {
        return new JmxConnection(null, ManagementFactory.getPlatformMBeanServer());
    }
}
