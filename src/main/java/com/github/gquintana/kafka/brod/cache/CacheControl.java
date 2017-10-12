package com.github.gquintana.kafka.brod.cache;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Enabled HTTP Caching
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheControl {
    /**
     * Cache-Control HTTP header value
     */
    String value() default "max-age=10s";
}
