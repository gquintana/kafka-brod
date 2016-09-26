package com.github.gquintana.kafka.brod;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/topics")
@Produces(MediaType.APPLICATION_JSON)
public class TopicResource {
    private final TopicService topicService;

    public TopicResource(TopicService topicService) {
        this.topicService = topicService;
    }

    /**
     * Get topic detailed info
     */
    @GET
    public List<String> getTopics() {
        return topicService.getTopics();
    }

    /**
     * Get topic detailed info
     */
    @GET
    @Path("/:name")
    public Response getTopic(@PathParam("name") String name) {
        return Responses.of(topicService.getTopic(name));
    }
}