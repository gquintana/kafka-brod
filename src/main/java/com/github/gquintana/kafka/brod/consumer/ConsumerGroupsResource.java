package com.github.gquintana.kafka.brod.consumer;

import com.github.gquintana.kafka.brod.Resources;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Api
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
    public List<String> getGroupIds(@QueryParam("broker_id") Integer brokerId) {
        return brokerId == null ? groupService.getGroupIds() : groupService.getGroupIds(brokerId);
    }

    /**
     * Get consumer group resource
     */
    @Path("{id}")
    public ConsumerGroupResource getGroup(@PathParam("id") String id) {
        return resources.consumerGroupResource(id);
    }
}
