package com.github.gquintana.kafka.brod.jmx;

import java.util.*;

public class JmxQuery {
    private final List<Element> elements;

    private JmxQuery(List<Element> elements) {
        this.elements = Collections.unmodifiableList(elements);
    }

    private static class Element {
        private final String mBeanName;
        private final String[] attributeNames;

        private Element(String mBeanName, String... attributeNames) {
            this.mBeanName = mBeanName;
            this.attributeNames = attributeNames;
        }

        private Map<String, Object> execute(JmxConnection connection) {
            return connection.getAttributes(mBeanName, attributeNames);
        }
    }

    public static class Builder {
        private final List<Element> elements = new ArrayList<>();

        public Builder withAttributes(String mBeanName, String... attributeNames) {
            elements.add(new Element(mBeanName, attributeNames));
            return this;
        }

        public Builder withRateAttributes(String mBeanName) {
            return withAttributes(mBeanName, "OneMinuteRate", "FiveMinuteRate");
        }

        public Builder withJavaAttributes() {
            return withAttributes("java.lang:type=Memory","HeapMemoryUsage", "NonHeapMemoryUsage")
                .withAttributes("java.lang:type=Threading", "ThreadCount")
                .withAttributes("java.lang:type=OperatingSystem", "SystemLoadAverage", "SystemCpuLoad", "ProcessCpuLoad", "OpenFileDescriptorCount", "MaxFileDescriptorCount", "FreeSwapSpaceSize", "FreePhysicalMemorySize", "CommittedVirtualMemorySize");
        }

        public Builder withJavaCmsGcAttributes() {
            return withAttributes("java.lang:type=GarbageCollector,name=ConcurrentMarkSweep", "CollectionCount", "CollectionTime");
        }
        public JmxQuery build() {
            return new JmxQuery(elements);
        }
    }

    public Map<String, Object> execute(JmxConnection connection) {
        Map<String, Object> result = new HashMap<>();
        for (Element element : elements) {
            try {
                result.putAll(element.execute(connection));
            } catch (JmxException e) {
                // TODO
            }
        }
        return result;
    }
}
