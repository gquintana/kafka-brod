package com.github.gquintana.kafka.brod;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class Configuration {
    private final Map<String, Object> config = new HashMap<>();

    public void loadSystem() throws IOException {
        setProperties(System.getProperties());
    }

    public void load(String path) throws IOException {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource not found: " + path);
            }
            load(inputStream);
        }
    }

    public void load(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            load(inputStream);
        }
    }

    public void load(URL url) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            load(inputStream);
        }
    }

    private void load(InputStream inputStream) throws IOException {
        Properties properties = new Properties();
        properties.load(inputStream);
        setProperties(properties);
    }

    private void setProperties(Properties properties) {
        config.putAll(properties.entrySet().stream()
            .collect(Collectors.toMap(
                p -> p.getKey().toString(),
                p -> p.getValue())));
    }

    public Map<String, Object> getAsMap() {
        return config;
    }

    public Optional<Object> get(String key) {
        return Optional.ofNullable(config.get(key));
    }

    public Optional<String> getAsString(String key) {
        return get(key).map(Object::toString);
    }

    private static Integer toInteger(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj instanceof String) {
            return Integer.parseInt(((String) obj).trim());
        } else {
            throw new KafkaBrodException("Can not convert to Integer " + obj);
        }
    }
    public Optional<Integer> getAsInteger(String key) {
        return get(key).map(Configuration::toInteger);
    }

    private static Boolean toBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return ((Boolean) obj);
        } else if (obj instanceof String) {
            return Boolean.parseBoolean((String) obj);
        } else {
            throw new KafkaBrodException("Can not convert to Boolean " + obj);
        }
    }

    public Optional<Boolean> getAsBoolean(String key) {
        return get(key).map(Configuration::toBoolean);
    }
}
