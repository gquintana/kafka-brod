package com.github.gquintana.kafka.brod.jmx;

import com.github.gquintana.kafka.brod.Configuration;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class JmxConfiguration {
    public static final int SO_TIMEOUT_DEFAULT = 3000;
    private final boolean ssl;
    private final Integer port;
    private final String user;
    private final String password;
    private final int soTimeout;

    public boolean isAuthentication() {
        return user != null && !user.isEmpty() && password != null && !password.isEmpty();
    }

    public static JmxConfiguration create(Configuration configuration, String keyPrefix) {
        return new JmxConfiguration(
            configuration.getAsBoolean(keyPrefix + ".jmx.ssl").orElse(false),
            configuration.getAsInteger(keyPrefix + ".jmx.port").orElse(null),
            configuration.getAsString(keyPrefix + ".jmx.user").orElse(null),
            configuration.getAsString(keyPrefix + ".jmx.password").orElse(null),
            configuration.getAsInteger(keyPrefix + ".jmx.soTimeout").orElse(SO_TIMEOUT_DEFAULT)
            );
    }
}
