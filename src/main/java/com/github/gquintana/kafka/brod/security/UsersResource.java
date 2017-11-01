package com.github.gquintana.kafka.brod.security;

import com.github.gquintana.kafka.brod.Resources;
import io.swagger.annotations.Api;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Api(tags = {"user"})
@Produces(MediaType.APPLICATION_JSON)
public class UsersResource {
    private final Resources resources;

    public UsersResource(Resources resources) {
        this.resources = resources;
    }

    @Path("{name}")
    public UserResource getUser(@PathParam("name") String name) {
        return resources.userResource(name);
    }
}
