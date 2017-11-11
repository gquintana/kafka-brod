package com.github.gquintana.kafka.brod.task;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class MoreExecutors {
    private static class NamedThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private NamedThreadFactory(String name) {
            this.namePrefix = name + "-";
            this.group = new ThreadGroup(namePrefix + "group");
        }

        public Thread newThread(Runnable runnable) {
            return new Thread(this.group, runnable, this.namePrefix + this.threadNumber.getAndIncrement());
        }
    }

    public static ThreadPoolExecutor threadPool(String name, int queueSize, int threadCount) {
        RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
        ThreadFactory threadFactory = new NamedThreadFactory(name);
        return new ThreadPoolExecutor(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(queueSize), threadFactory, rejectedExecutionHandler);
    }
}
