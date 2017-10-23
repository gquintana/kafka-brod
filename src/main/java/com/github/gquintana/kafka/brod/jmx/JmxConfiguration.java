package com.github.gquintana.kafka.brod.jmx;

public class JmxConfiguration {
    private final boolean ssl;
    private final String user;
    private final String password;

    public JmxConfiguration(boolean ssl, String user, String password) {
        this.ssl = ssl;
        this.user = user;
        this.password = password;
    }

    public boolean isSsl() {
        return ssl;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAuthentication() {
        return user != null && !user.isEmpty() && password != null && !password.isEmpty();
    }
}
