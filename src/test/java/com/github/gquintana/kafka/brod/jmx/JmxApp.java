package com.github.gquintana.kafka.brod.jmx;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JmxApp {
    private static boolean running;

    private static void log(String message) {
        System.out.println(String.format("%1$tH:%1$tM:%1$tS %2$s", new Date(), message));
    }
    public static void main(String[] args) throws InterruptedException {
        log(JmxApp.class.getSimpleName() + " started");
        running = true;
        long l=0;
        while (running) {
            if (l%10 ==0) {
                log(JmxApp.class.getSimpleName() + " is alive");
            }
            Thread.sleep(1000L);
            l++;
        }
    }
}
