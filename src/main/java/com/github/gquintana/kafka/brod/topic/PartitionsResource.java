package com.github.gquintana.kafka.brod.topic;

import com.github.gquintana.kafka.brod.Resources;
import com.github.gquintana.kafka.brod.Responses;
import com.github.gquintana.kafka.brod.broker.Broker;
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

@Api
@Produces(MediaType.APPLICATION_JSON)
public class PartitionsResource {
    private final Resources resources;
    private final PartitionService partitionService;
    private final String topicName;

    public PartitionsResource(Resources resources, PartitionService partitionService, String topicName) {
        this.resources = resources;
        this.partitionService = partitionService;
        this.topicName = topicName;
    }

    @GET
    @ApiOperation(value = "List topic partitions")
    public List<Partition> getPartitions() {
        return partitionService.getPartitions(topicName);
    }

    @GET
    @Path("{id}")
    @ApiOperation(value = "Get topic partition detail")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Topic partition found", response = Topic.class),
        @ApiResponse(code = 404, message = "Topic partition not found")
    })
    public Response getPartitions(@PathParam("id") int partitionId) {
        return Responses.of(partitionService.getPartition(topicName, partitionId));
    }
}
