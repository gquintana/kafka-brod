package com.github.gquintana.kafka.brod;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/brokers")
@Produces(MediaType.APPLICATION_JSON)
public class BrokerResource {
    private final BrokerService brokerService;

    public BrokerResource(BrokerService brokerService) {
        this.brokerService = brokerService;
    }

    /**
     * Get broker detailed info
     */
    @GET
    public List<Integer> getBrokers() {
        return brokerService.getBrokers();
    }

    /**
     * Get broker detailed info
     */
    @GET
    @Path("/:id")
    public Response getBroker(@PathParam("id") int id) {
        return Responses.of(brokerService.getBroker(id));
    }
}