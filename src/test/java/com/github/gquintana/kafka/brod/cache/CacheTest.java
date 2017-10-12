package com.github.gquintana.kafka.brod.cache;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
public class CacheTest {
    private final Cache<Integer, String> cache = new Cache<>(1000L);

    @Test
    public void testGetWhenFound() {
        cache.put(2, "Two");
        assertThat(cache.get(2).isPresent(), is(true));
        assertThat(cache.get(2).get(), equalTo("Two"));
    }

    @Test
    public void testGetWhenNotFound() {
        assertThat(cache.get(3).isPresent(), is(false));
    }

    @Test
    public void testGetWhenExpired() throws InterruptedException {
        cache.put(2, "Two");
        Thread.sleep(1100L);
        assertThat(cache.get(2).isPresent(), is(false));
    }

    @Test
    public void testPutWhenNullValue() throws InterruptedException {
        cache.put(2, null);
        assertThat(cache.get(2).isPresent(), is(false));
    }
}
