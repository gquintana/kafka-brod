package com.github.gquintana.kafka.brod.consumer;

import com.github.gquintana.kafka.brod.Resources;
import com.github.gquintana.kafka.brod.security.Roles;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Api(tags = {"consumer"})
@Produces(MediaType.APPLICATION_JSON)
public class ConsumerGroupsResource {
    private final Resources resources;
    private final ConsumerGroupService groupService;

    public ConsumerGroupsResource(Resources resources, ConsumerGroupService groupService) {
        this.resources = resources;
        this.groupService = groupService;
    }

    /**
     * Get consumer group ids list
     */
    @GET
    @ApiOperation(value = "List consumer groups")
    @RolesAllowed({Roles.USER})
    public List<String> getGroupIds() {
        return groupService.getGroupIds();
    }

    /**
     * Get consumer group resource
     */
    @Path("{id}")
    public ConsumerGroupResource getGroup(@PathParam("id") String id) {
        return resources.consumerGroupResource(id);
    }
}
