package com.github.gquintana.kafka.brod;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestResources {
    public static InputStream getResourceAsStream(String name) throws FileNotFoundException {
        InputStream inputStream = TestResources.class.getResourceAsStream(name);
        if (inputStream == null) {
            throw new FileNotFoundException("Resource " + name + " not found");
        }
        return inputStream;
    }

    public static Properties getResourceAsProperties(String name) throws IOException {
        try (InputStream inputStream = getResourceAsStream(name)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        }
    }
}
