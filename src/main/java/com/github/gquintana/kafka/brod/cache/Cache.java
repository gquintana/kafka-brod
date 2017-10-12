package com.github.gquintana.kafka.brod.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Cache<K, V> {
    private final Map<K, Entry<V>> entries = new HashMap<>();
    private final long timeToLive;

    public Cache(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    private static class Entry<V> {
        private final long expireTime;
        private final V value;

        private Entry(long expireTime, V value) {
            this.expireTime = expireTime;
            this.value = value;
        }

        private long getExpireTime() {
            return expireTime;
        }

        private V getValue() {
            return value;
        }
    }

    public synchronized void put(K key, V value) {
        if (value == null) {
            return;
        }
        this.entries.put(key, new Entry<V>(getTime() + timeToLive, value));
    }

    long getTime() {
        return System.currentTimeMillis();
    }

    public synchronized Optional<V> get(K key) {
        Entry<V> entry = entries.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (getTime() > entry.getExpireTime()) {
            entries.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.getValue());
    }

}
