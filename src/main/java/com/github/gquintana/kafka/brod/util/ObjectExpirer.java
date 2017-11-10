package com.github.gquintana.kafka.brod.util;

import java.util.function.Supplier;

/**
 * Forces connections to be recreated from time to time to avoid stale connections
 */
public class ObjectExpirer<T> implements AutoCloseable, Supplier<T> {
    private final Supplier<T> supplier;
    private final long timeToLive;

    public ObjectExpirer(Supplier<T> supplier, long timeToLive) {
        this.supplier = supplier;
        this.timeToLive = timeToLive;
    }

    private T object;
    private long expirationTime;

    public T get() {
        T oldObject = initIfNeeded();
        closeIfNeeded(oldObject);
        return object;
    }

    private synchronized T initIfNeeded() {
        T oldObject = null;
        long now = System.currentTimeMillis();
        if (object == null || expirationTime < now) {
            oldObject = this.object;
            this.object = supplier.get();
            this.expirationTime = now + timeToLive;
        }
        return oldObject;
    }

    private void closeIfNeeded(T oldObject) {
        if (oldObject instanceof AutoCloseable) {
            try {
                ((AutoCloseable) oldObject).close();
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void close() {
        closeIfNeeded(object);
        this.object = null;
    }
}
