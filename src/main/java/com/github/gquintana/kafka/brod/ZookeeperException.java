package com.github.gquintana.kafka.brod;

public class ZookeeperException extends KafkaBrodException {
    public ZookeeperException(String message) {
        super(message);
    }

    public ZookeeperException(String message, Throwable cause) {
        super(message, cause);
    }
}
