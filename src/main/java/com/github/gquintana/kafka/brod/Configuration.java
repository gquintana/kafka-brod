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

    public Configuration() {
    }

    public Configuration(Map<String, Object> config) {
        this.config.putAll(config);
    }

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
                Map.Entry::getValue)));
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

    private static Long toLong(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        } else if (obj instanceof String) {
            return Long.parseLong(((String) obj).trim());
        } else {
            throw new KafkaBrodException("Can not convert to Long " + obj);
        }
    }

    public Optional<Long> getAsLong(String key) {
        return get(key).map(Configuration::toLong);
    }

    private static Class toClass(Object obj) {
        if (obj instanceof Class) {
            return ((Class) obj);
        } else if (obj instanceof String) {
            try {
                return Class.forName((String) obj);
            } catch (ClassNotFoundException e) {
                throw new KafkaBrodException("Can not convert to Class " + obj, e);
            }
        } else {
            throw new KafkaBrodException("Can not convert to Class " + obj);
        }
    }

    public Optional<Class> getAsClass(String key) {
        return get(key).map(Configuration::toClass);
    }

    public Configuration getAsConfiguration(String key) {
        String prefix = key + ".";
        Map<String, Object> subConfig = config.entrySet().stream()
            .filter(e -> e.getKey().startsWith(prefix))
            .collect(Collectors.toMap(
                e -> e.getKey().substring(prefix.length()),
                Map.Entry::getValue
            ));
        return new Configuration(subConfig);
    }
}
