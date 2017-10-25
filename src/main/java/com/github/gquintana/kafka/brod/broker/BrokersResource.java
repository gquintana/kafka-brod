package com.github.gquintana.kafka.brod.broker;

import com.github.gquintana.kafka.brod.Resources;
import com.github.gquintana.kafka.brod.Responses;
import com.github.gquintana.kafka.brod.cache.CacheControl;
import com.github.gquintana.kafka.brod.security.Roles;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Api(tags = {"broker"})
@Produces(MediaType.APPLICATION_JSON)
public class BrokersResource {
    private final Resources resources;
    private final BrokerService brokerService;
    private final BrokerJmxService brokerJmxService;

    public BrokersResource(Resources resources, BrokerService brokerService, BrokerJmxService brokerJmxService) {
        this.resources = resources;
        this.brokerService = brokerService;
        this.brokerJmxService = brokerJmxService;
    }

    /**
     * List brokers
     */
    @GET
    @ApiOperation(value = "List broker ids")
    @CacheControl("max-age=60")
    @RolesAllowed({Roles.USER})
    public List<Broker> getBrokers() {
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
    @RolesAllowed({Roles.USER})
    public Response getBroker(@PathParam("id") int id) {
        Optional<Broker> optBroker = brokerService.getBroker(id);
        optBroker = optBroker.map(b -> brokerJmxService.enrich(b));
        return Responses.of(optBroker);
    }
}
