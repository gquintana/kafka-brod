package com.github.gquintana.kafka.brod.broker;

import com.github.gquintana.kafka.brod.Resources;
import com.github.gquintana.kafka.brod.Responses;
import com.github.gquintana.kafka.brod.cache.Cache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(tags = {"broker"})
@Produces(MediaType.APPLICATION_JSON)
public class BrokersResource {
    private final Resources resources;
    private final BrokerService brokerService;

    public BrokersResource(Resources resources, BrokerService brokerService) {
        this.resources = resources;
        this.brokerService = brokerService;
    }

    /**
     * List brokers
     */
    @GET
    @ApiOperation(value = "List broker ids")
    @Cache("max-age=60")
    public List<Integer> getBrokers() {
        return brokerService.getBrokers();
    }

    /**
     * Get broker detailed info
     */
    @GET
    @ApiOperation(value = "Get broker detail")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Broker found", response = Broker.class),
        @ApiResponse(code = 404, message = "Broker not found")
    })
    @Path("{id}")
    public Response getBroker(@PathParam("id") int id) {
        return Responses.of(brokerService.getBroker(id));
    }
}
