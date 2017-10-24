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
        int status;
        if (ex instanceof WebApplicationException) {
            status = ((WebApplicationException) ex).getResponse().getStatus();
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        }
        Error error = new Error(ex.getMessage(), ex.getClass().getName(), status);
        return Response.status(error.getStatus())
            .entity(error)
            .build();
    }
}
