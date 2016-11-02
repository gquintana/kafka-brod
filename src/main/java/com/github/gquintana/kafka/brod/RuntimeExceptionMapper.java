package com.github.gquintana.kafka.brod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException ex) {
        LOGGER.error("Runtime exception", ex);
        Error error = new Error();
        error.setMessage(ex.getMessage());
        error.setClassName(ex.getClass().getName());
        if (ex instanceof WebApplicationException) {
            error.setStatus(((WebApplicationException) ex).getResponse().getStatus());
        } else {
            error.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        return Response.status(error.getStatus())
            .entity(error)
            .build();
    }
}
