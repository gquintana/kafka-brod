package com.github.gquintana.kafka.brod.cache;

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CacheTest {
    private int count = 0;
    private final Cache<Integer, String> cache = new Cache<>(this::load, 1000L);

    private Optional<String> load(Integer id) {
        switch (id) {
            case 1:
                return Optional.of("One#"+(count++));
            case 2:
                return Optional.of("Two#"+(count++));
            case 3:
                return Optional.of("Three#"+(count++));
            default:
                return Optional.empty();

        }
    }

    @Test
    public void testGetWhenFound() {
        Optional<String> two = cache.get(2);
        assertThat(two.isPresent(), is(true));
        assertThat(two.get(), equalTo("Two#0"));
        Optional<String> three = cache.get(3);
        assertThat(three.isPresent(), is(true));
        assertThat(three.get(), equalTo("Three#1"));
    }

    @Test
    public void testGetWhenNotFound() {
        assertThat(cache.get(4).isPresent(), is(false));
    }

    @Test
    public void testGetWhenExpired() throws InterruptedException {
        Optional<String> two = cache.get(2);
        assertThat(two.get(), equalTo("Two#0"));
        Thread.sleep(1100L);
        two = cache.get(2);
        assertThat(two.isPresent(), is(true));
        assertThat(two.get(), equalTo("Two#1"));
    }
}
