package com.github.gquintana.kafka.brod.jmx;

public class JmxAttributes {
    private final String mBeanName;
    private final String[] attributeNames;

    public JmxAttributes(String mBeanName, String[] attributeNames) {
        this.mBeanName = mBeanName;
        this.attributeNames = attributeNames;
    }

    public String getmBeanName() {
        return mBeanName;
    }

    public String[] getAttributeNames() {
        return attributeNames;
    }
}
