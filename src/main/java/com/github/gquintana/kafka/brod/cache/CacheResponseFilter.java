package com.github.gquintana.kafka.brod.cache;

import javax.ws.rs.GET;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Response filter which adds Cache-Control header
 */
public class CacheResponseFilter implements ContainerResponseFilter {
    /**
     * Default Cache-Control HTTP header value for GET requests
     */
    private final String cacheControlHeader;

    public CacheResponseFilter() {
        cacheControlHeader = "max-age=10";
    }

    public CacheResponseFilter(String cacheControlHeader) {
        this.cacheControlHeader = cacheControlHeader;
    }

    private static <T> Optional<T> findAnnotation(ContainerResponseContext responseContext, Class<T> annotationClass) {
        return Arrays.stream(responseContext.getEntityAnnotations())
            .filter(annotationClass::isInstance)
            .map(annotationClass::cast)
            .findFirst();
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        Optional<Cache> cacheAnnotation = findAnnotation(responseContext, Cache.class);
        if (cacheAnnotation.isPresent()) {
            responseContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, cacheAnnotation.get().value());
            return;
        }
        Optional<GET> getAnnotation = findAnnotation(responseContext, GET.class);
        if (getAnnotation.isPresent()) {
            responseContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, cacheControlHeader);
        }
    }
}
