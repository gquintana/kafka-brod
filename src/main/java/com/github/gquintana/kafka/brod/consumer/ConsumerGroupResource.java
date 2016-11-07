package com.github.gquintana.kafka.brod.consumer;

import com.github.gquintana.kafka.brod.Resources;
import com.github.gquintana.kafka.brod.Responses;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
     * @param topic Optional topic name to filter assigned topics
     */
    @GET
    public Response getGroup(@QueryParam("topic") String topic) {
        return Responses.of(groupService.getGroup(groupId, topic));
    }

}
