package com.github.gquintana.kafka.brod;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
public class ConsumerGroupResource {
    private final Resources resources;
    private final ConsumerGroupService groupService;
    private final String groupId;

    public ConsumerGroupResource(Resources resources, ConsumerGroupService groupService, String groupId) {
        this.resources = resources;
        this.groupService = groupService;
        this.groupId = groupId;
    }

    /**
     * Get consumer group detailed info
     */
    @GET
    public Response getGroup() {
        return Responses.of(groupService.getGroup(groupId));
    }

}
