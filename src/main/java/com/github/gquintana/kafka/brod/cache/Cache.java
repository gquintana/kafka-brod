package com.github.gquintana.kafka.brod.cache;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class Cache<K, V> {
    private final Map<K, Entry<V>> entries = new HashMap<>();
    private final Function<K, Optional<V>> supplier;
    private final long timeToLive;

    public Cache(Function<K, Optional<V>> supplier, long timeToLive) {
        this.supplier = supplier;
        this.timeToLive = timeToLive;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Entry<V> {
        private final long expirationTime;
        private final V value;

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }

    long getTime() {
        return System.currentTimeMillis();
    }

    public synchronized Optional<V> get(K key) {
        Entry<V> entry = entries.get(key);
        if (entry != null && !entry.isExpired()) {
            return Optional.ofNullable(entry.value);
        }
        Optional<V> value = supplier.apply(key);
        if (value.isPresent()) {
            this.entries.put(key, new Entry<>(getTime() + timeToLive, value.get()));
            return value;
        }
        if (entry != null) {
            entries.remove(key);
        }
        return Optional.empty();
    }

}
