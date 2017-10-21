package com.github.gquintana.kafka.brod.jmx;

import com.github.gquintana.kafka.brod.util.Strings;
import scala.collection.mutable.StringBuilder;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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

    public Map<String, Object> getAttributes(String mBeanName, String... attributeNames) {
        try {
            ObjectName objectName = new ObjectName(mBeanName);
            String keyPrefix = buildKeyPrefix(objectName);
            AttributesVisitor visitor = new AttributesVisitor();
            for(Object attribute: mBeanServerConnection.getAttributes(objectName, attributeNames)) {
                visitor.visit(keyPrefix, (Attribute) attribute);
            }
            return visitor.result;
        } catch (IOException | JMException e) {
            throw new JmxException("Failed to get " + mBeanName + " " + attributeNames, e);
        }
    }

    private static class AttributesVisitor {
        private final Map<String, Object> result = new HashMap<>();

        private void visit(String keyPrefix, Attribute attribute) {
            visit(keyPrefix + Strings.toSnakeCase(attribute.getName()), attribute.getValue());
        }

        private void visit(String key, Object value) {
            if (value == null) {
                return;
            }
            if (value instanceof Number || value instanceof String || value instanceof Boolean) {
                result.put(key, value);
            } else if (value instanceof CompositeData) {
                CompositeData compositeValue = (CompositeData) value;
                for (String compositeValueField : compositeValue.getCompositeType().keySet()) {
                    visit(key + "." + compositeValueField, compositeValue.get(compositeValueField));
                }
            }
        }
    }

    private static String buildKeyPrefix(ObjectName objectName) {
        StringBuilder keyPrefixBuilder = new StringBuilder()
            .append(Strings.toSnakeCase(objectName.getDomain()))
            .append('.');
        String type = objectName.getKeyProperty("type");
        if (type != null) {
            keyPrefixBuilder.append(Strings.toSnakeCase(type)).append('.');
        }
        String name = objectName.getKeyProperty("name");
        if (name != null) {
            keyPrefixBuilder.append(Strings.toSnakeCase(name)).append('.');
        }
        return keyPrefixBuilder.toString();
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
