package com.github.gquintana.kafka.brod.jmx;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class JmxConfiguration {
    private final boolean ssl;
    private final String user;
    private final String password;

    public boolean isAuthentication() {
        return user != null && !user.isEmpty() && password != null && !password.isEmpty();
    }
}
