package com.github.gquintana.kafka.brod;

import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * Helper class to create @{link Response}s
 */
public final class Responses {
    private Responses() {
    }

    /**
     * Convert optional to HTTP status 200 or 404
     */
    public static <T> Response of(Optional<T> optional) {
        Response.ResponseBuilder responseBuilder;
        if (optional.isPresent()) {
            responseBuilder = Response.ok(optional.get());
        } else {
            responseBuilder = Response.noContent();
        }
        return responseBuilder.build();
    }
}
