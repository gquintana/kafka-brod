package com.github.gquintana.kafka.brod;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/groups")
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
    public List<String> getGroupIds(@QueryParam("broker_id") Integer brokerId) {
        return brokerId == null ? groupService.getGroupIds() : groupService.getGroupIds(brokerId);
    }

    /**
     * Get consumer group resource
     */
    @Path("{id}")
    public ConsumerGroupResource getTopic(@PathParam("id") String id) {
        return resources.consumerGroupResource(id);
    }
}
