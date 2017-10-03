package com.github.gquintana.kafka.brod;

import io.swagger.annotations.Api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

@Path("/") @Api
public class KafkaBrodResource {
    private Resources resources;

    public KafkaBrodResource(Resources resources) {
        this.resources = resources;
    }

    @Path("api")
    public ApiResource getApi() {
        return resources.apiResource();
    }

    @Path("ui")
    public StaticResource getUi() {
        return resources.uiResource();
    }

    /**
     * Redirect / to static/index.html
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getIndex() throws URISyntaxException {
        return Response.seeOther(new URI("ui/index.html")).build();
    }
}
