package com.github.gquintana.kafka.brod.consumer;

import com.github.gquintana.kafka.brod.Resources;
import com.github.gquintana.kafka.brod.Responses;
import com.github.gquintana.kafka.brod.security.Roles;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = {"consumer"})
@Produces(MediaType.APPLICATION_JSON)
public class ConsumerResource {
    private final Resources resources;
    private final ConsumerGroupService consumerService;
    private final String groupId;
    private final String consumerId;

    public ConsumerResource(Resources resources, ConsumerGroupService consumerService, String groupId, String consumerId) {
        this.resources = resources;
        this.consumerService = consumerService;
        this.groupId = groupId;
        this.consumerId = consumerId;
    }

    /**
     * Get consumer detailed info
     * @param topic Optional topic name to filter assigned topics
     */
    @GET
    @ApiOperation(value = "Get consumer detail")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Consumer found", response = ConsumerGroup.class),
        @ApiResponse(code = 404, message = "Consumer not found")
    })
    @RolesAllowed({Roles.USER})
    public Response getConsumer(@QueryParam("topic") String topic) {
        return Responses.of(consumerService.getConsumer(groupId, consumerId, topic));
    }
}
