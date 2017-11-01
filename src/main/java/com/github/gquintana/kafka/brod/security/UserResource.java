package com.github.gquintana.kafka.brod.security;

import com.github.gquintana.kafka.brod.Responses;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Optional;

@Api(tags = {"user"})
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    private final UserService userService;
    private final JwtService jwtService;
    private final String userName;

    public UserResource(UserService userService, JwtService jwtService, String userName) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.userName = userName;
    }


    /**
     * Authenticate user
     */
    @POST
    @Path("_auth")
    @ApiOperation(value = "Authenticate user")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Authentication success"),
        @ApiResponse(code = 401, message = "Authentication failed")
    })
    @Consumes(MediaType.TEXT_PLAIN)
    @PermitAll
    public Response authenticate(String password) {
        if (userService == null) {
            return Response.noContent().build();
        }
        Response.ResponseBuilder responseBuilder;
        if (userService.authenticate(userName, password)) {
            String jwtToken = jwtService.createToken(userName);
            Optional<User> optUser = userService.getUser(userName);
            responseBuilder = Response.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken).entity(optUser.get());
        } else {
            responseBuilder = Response.status(Response.Status.UNAUTHORIZED);
        }
        return responseBuilder.build();
    }

    /**
     * Authenticate user
     */
    @GET
    @ApiOperation(value = "Get user detail")
    @ApiResponses({
        @ApiResponse(code = 200, message = "User found", response = User.class),
        @ApiResponse(code = 404, message = "User not found", response = User.class),
        @ApiResponse(code = 403, message = "Forbidden")
    })
    @Consumes(MediaType.TEXT_PLAIN)
    public Response get(@Context SecurityContext securityContext) {
        if (userService == null) {
            return Response.noContent().build();
        }
        if (!securityContext.isUserInRole(Roles.ADMIN) && !userName.equals(securityContext.getUserPrincipal().getName())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Responses.of(userService.getUser(userName));
    }
}
