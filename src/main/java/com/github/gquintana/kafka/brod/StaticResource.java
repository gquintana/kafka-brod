package com.github.gquintana.kafka.brod;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StaticResource {
    @GET
    @Path("{path:.+\\.html}")
    @Produces(MediaType.TEXT_HTML)
    public Response getHtml(@PathParam("path") String path) {
        return get(path);
    }

    @GET
    @Path("{path:.+\\.css}")
    @Produces("text/css")
    public Response getCss(@PathParam("path") String path) {
        return get(path);
    }

    @GET
    @Path("{path:.+\\.js}")
    @Produces("application/js")
    public Response getJs(@PathParam("path") String path) {
        return get(path);
    }

    @GET
    @Path("{path:.+\\.png}")
    @Produces("image/png")
    public Response getPng(@PathParam("path") String path) {
        return get(path);
    }

    @GET
    @Path("{path:.+\\.jpe?g}")
    @Produces("image/jpeg")
    public Response getJpeg(@PathParam("path") String path) {
        return get(path);
    }

    private Response get(String path) {
        StreamingOutput streamingOutput = new ResourceStreamingOutput("static/" + path);
        return Response.ok().entity(streamingOutput).build();
    }

    private static class ResourceStreamingOutput implements StreamingOutput {
        final InputStream inputStream;

        private ResourceStreamingOutput(String resource) {
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            if (inputStream == null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }

        @Override
        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
            try {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
            } finally {
                inputStream.close();
            }
        }
    }
}
