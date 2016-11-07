package com.github.gquintana.kafka.brod.broker;

import com.github.gquintana.kafka.brod.Resources;
import com.github.gquintana.kafka.brod.Responses;
import com.github.gquintana.kafka.brod.cache.Cache;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/brokers")
@Produces(MediaType.APPLICATION_JSON)
public class BrokersResource {
    private final Resources resources;
    private final BrokerService brokerService;

    public BrokersResource(Resources resources, BrokerService brokerService) {
        this.resources = resources;
        this.brokerService = brokerService;
    }

    /**
     * Get broker detailed info
     */
    @GET
    @Cache("max-age=60")
    public List<Integer> getBrokers() {
        return brokerService.getBrokers();
    }

    /**
     * Get broker detailed info
     */
    @GET
    @Path("{id}")
    public Response getBroker(@PathParam("id") int id) {
        return Responses.of(brokerService.getBroker(id));
    }
}
