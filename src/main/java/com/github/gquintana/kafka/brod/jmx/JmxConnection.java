package com.github.gquintana.kafka.brod.jmx;

import com.github.gquintana.kafka.brod.util.Strings;

import javax.management.*;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public class JmxConnection implements AutoCloseable {
    private final JMXConnector jmxConnector;
    private final MBeanServerConnection mBeanServerConnection;

    public JmxConnection(JMXConnector jmxConnector, MBeanServerConnection mBeanServerConnection) {
        this.jmxConnector = jmxConnector;
        this.mBeanServerConnection = mBeanServerConnection;
    }

    public String getId() {
        try {
            return this.jmxConnector.getConnectionId();
        } catch (IOException e) {
            throw new JmxException("Failed to get connection id", e);
        }
    }

    public Object getAttribute(String mBeanName, String attributeName) {
        try {
            ObjectName objectName = new ObjectName(mBeanName);
            return mBeanServerConnection.getAttribute(objectName, attributeName);
        } catch (IOException | JMException e) {
            throw new JmxException("Failed to get " + mBeanName + " " + attributeName, e);
        }
    }

    public Map<String, Object> getAttributes(String mBeanName, String... attributeNames) {
        try {
            ObjectName objectName = new ObjectName(mBeanName);
            return mBeanServerConnection.getAttributes(objectName, attributeNames).stream()
                .map(o -> (Attribute) o)
                .filter(a -> a.getValue() != null)
                .collect(toMap(new AttributeKeyFunction(objectName), Attribute::getValue));
        } catch (IOException | JMException e) {
            throw new JmxException("Failed to get " + mBeanName + " " + attributeNames, e);
        }
    }

    private static class AttributeKeyFunction implements Function<Attribute, String> {
        private final String keyDomain;
        private final String keyObject;
        private AttributeKeyFunction(ObjectName objectName) {
            keyDomain = Strings.toSnakeCase(objectName.getDomain());
            String lKeyObject = objectName.getKeyProperty("name");
            if (lKeyObject == null) {
                lKeyObject = objectName.getKeyPropertyListString();
            }
            lKeyObject = Strings.toSnakeCase(lKeyObject);
            keyObject = lKeyObject;
        }

        @Override
        public String apply(Attribute attribute) {
            String keyAttribute = attribute.getName();
            return Stream.of(keyDomain, keyObject, keyAttribute).collect(joining("."));
        }
    }

    public Map<String, Object> getRateAttributes(String mBeanName) {
        return getAttributes(mBeanName, "OneMinuteRate", "FiveMinuteRate");
    }

    @Override
    public void close() {
        try {
            jmxConnector.close();
        } catch (IOException e) {
            throw new JmxException("Failed to close connection", e);
        }
    }
}
