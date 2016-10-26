package com.github.gquintana.kafka.brod;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    public Response getTopic() {
        return Responses.of(topicService.getTopic(topicName));
    }

    /**
     * Create/update topic
     */
    @PUT
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
