package com.github.gquintana.kafka.brod.jmx;

import com.github.gquintana.kafka.brod.KafkaBrodException;

public class JmxException extends KafkaBrodException {
    public JmxException(String message) {
        super(message);
    }

    public JmxException(String message, Throwable cause) {
        super(message, cause);
    }
}
