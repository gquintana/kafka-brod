package com.github.gquintana.kafka.brod;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class Error {
    private final String message;
    private final String className;
    private final int status;
}
