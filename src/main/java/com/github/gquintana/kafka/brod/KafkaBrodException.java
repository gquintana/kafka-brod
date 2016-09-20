package com.github.gquintana.kafka.brod;

public class KafkaBrodException extends RuntimeException {
    public KafkaBrodException(String message) {
        super(message);
    }

    public KafkaBrodException(String message, Throwable cause) {
        super(message, cause);
    }
}
