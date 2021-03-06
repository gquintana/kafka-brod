package com.github.gquintana.kafka.brod.topic;

import com.github.gquintana.kafka.brod.Resources;
import com.github.gquintana.kafka.brod.Responses;
import com.github.gquintana.kafka.brod.security.Roles;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = {"topic"})
@Produces(MediaType.APPLICATION_JSON)
public class TopicResource {
    private final Resources resources;
    private final TopicService topicService;
    private final String topicName;

    public TopicResource(Resources resources, TopicService topicService, String topicName) {
        this.resources = resources;
        this.topicService = topicService;
        this.topicName = topicName;
    }

    /**
     * Get topic detailed info
     */
    @GET
    @ApiOperation(value = "Get topic detail")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Topic found", response = Topic.class),
        @ApiResponse(code = 404, message = "Topic not found")
    })
    @RolesAllowed({Roles.USER})
    public Response getTopic() {
        return Responses.of(topicService.getTopic(topicName));
    }

    /**
     * Create/update topic
     */
    @PUT
    @ApiOperation(value = "Create/update topic")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"ADMIN"})
    public Response createOrUpdateTopic(Topic topic) {
        if (topicName.equals(topic.getName())) {
            throw new WebApplicationException("Invalid topic name", Response.Status.BAD_REQUEST);
        }
        if (topicService.existTopic(topicName)) {
            topicService.updateTopic(topic);
        } else {
            topicService.createTopic(topic);
        }
        return Response.ok().build();
    }

    /**
     * Delete topic
     */
    @DELETE
    @ApiOperation(value = "Delete topic")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"ADMIN"})
    public Response deleteTopic() {
        if (topicService.existTopic(topicName)) {
            topicService.deleteTopic(topicName);
            return Response.ok().build();
        } else {
            return Response.noContent().build();
        }
    }
}
