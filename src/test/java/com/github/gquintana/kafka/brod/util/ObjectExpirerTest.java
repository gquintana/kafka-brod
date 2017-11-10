package com.github.gquintana.kafka.brod.util;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ObjectExpirerTest {
    private int currentInt = 0;

    @Test
    public void testDontExpireWhenUsed() throws Exception {
        // Given
        ObjectExpirer<Integer> expiringInt = new ObjectExpirer<>(this::intSupplier, 200L);
        int threads = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        // When
        List<UsingIntRunnable> runnables = IntStream.range(0, threads)
            .mapToObj(i -> new UsingIntRunnable(expiringInt))
            .collect(toList());
        runnables.forEach(executorService::submit);
        Thread.sleep(600L);
        runnables.forEach(r -> r.stop());
        // Then
        runnables.forEach(r -> assertThat(r.getInts().size(), equalTo(1)));
    }

    @Test
    public void testExpireWhenNotUsed() throws Exception {
        // Given
        ObjectExpirer<Integer> expiringInt = new ObjectExpirer<>(this::intSupplier, 200L);
        // When
        expiringInt.get();
        Thread.sleep(400L);
        int i = expiringInt.get();
        // Then
        assertThat(i, equalTo(1));
    }

    private Integer intSupplier() {
        return currentInt++;
    }
    private static class UsingIntRunnable implements Runnable {
        private final AtomicBoolean running = new AtomicBoolean();
        private final Set<Integer> ints = new HashSet<>();
        private final ObjectExpirer<Integer> intExpirer;

        public UsingIntRunnable(ObjectExpirer<Integer> intExpirer) {
            this.intExpirer = intExpirer;
        }

        @Override
        public void run() {
            running.set(true);
            while (running.get()) {
                ints.add(intExpirer.get());
            }
        }

        public void stop() {
            running.set(false);
        }

        public Set<Integer> getInts() {
            return ints;
        }
    }
}
