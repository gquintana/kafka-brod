package com.github.gquintana.kafka.brod;

import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.load("brod.properties");
        if (args.length > 0 ) {
            configuration.load(new File(args[0]));
        }
        configuration.loadSystem();
        new KafkaBrodApplication(configuration).run();
    }
}
