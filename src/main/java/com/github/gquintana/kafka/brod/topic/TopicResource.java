package com.github.gquintana.kafka.brod.topic;

import com.github.gquintana.kafka.brod.Resources;
import com.github.gquintana.kafka.brod.Responses;
import com.github.gquintana.kafka.brod.broker.Broker;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api
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

    @Path("partitions")
    public PartitionsResource getPartitions() {
        return resources.partitionsResource(topicName);

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
    public Response getTopic() {
        return Responses.of(topicService.getTopic(topicName));
    }

    /**
     * Create/update topic
     */
    @PUT
    @ApiOperation(value = "Create/update topic")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOrUpdateTopic(Topic topic) {
        topic.setName(topicName);
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
    public Response deleteTopic() {
        if (topicService.existTopic(topicName)) {
            topicService.deleteTopic(topicName);
            return Response.ok().build();
        } else {
            return Response.noContent().build();
        }
    }
}
