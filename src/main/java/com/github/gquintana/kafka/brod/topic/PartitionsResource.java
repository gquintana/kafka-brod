package com.github.gquintana.kafka.brod.topic;

import com.github.gquintana.kafka.brod.Resources;
import com.github.gquintana.kafka.brod.Responses;
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

@Api(tags = {"topic", "partition"})
@Produces(MediaType.APPLICATION_JSON)
public class PartitionsResource {
    private final Resources resources;
    private final PartitionService partitionService;
    private final PartitionJmxService partitionJmxService;
    private final String topicName;

    public PartitionsResource(Resources resources, PartitionService partitionService, PartitionJmxService partitionJmxService, String topicName) {
        this.resources = resources;
        this.partitionService = partitionService;
        this.partitionJmxService = partitionJmxService;
        this.topicName = topicName;
    }

    @GET
    @ApiOperation(value = "List topic partitions")
    @RolesAllowed({Roles.USER})
    public List<Partition> getPartitions() {
        List<Partition> partitions = partitionService.getPartitions(topicName);
        partitionJmxService.enrich(partitions);
        return partitions;
    }

    @GET
    @Path("{id}")
    @ApiOperation(value = "Get topic partition detail")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Topic partition found", response = Topic.class),
        @ApiResponse(code = 404, message = "Topic partition not found")
    })
    @RolesAllowed({Roles.USER})
    public Response getPartitions(@PathParam("id") int partitionId) {
        Optional<Partition> optPartition = partitionService.getPartition(topicName, partitionId)
            .map(partitionJmxService::enrich);
        return Responses.of(optPartition);
    }
}
