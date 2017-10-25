package com.github.gquintana.kafka.brod.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import java.util.HashSet;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserResourceTest {
    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    private UserResource userResource;

    @Before
    public void setUp() {
        userResource = new UserResource(userService, jwtService, "robert");
    }
    @Test
    public void testAuthenticateSuccess() throws Exception {
        // Given
        when(userService.authenticate(eq("robert"), eq("bobby123"))).thenReturn(true);
        when(jwtService.createToken(eq("robert"))).thenReturn("cafe.babe");
        when(userService.getUser(eq("robert"))).thenReturn(Optional.of(new User("robert", new HashSet<>(asList("USER")))));
        // When
        Response response = userResource.authenticate("bobby123");
        // Then
        assertThat(response.getStatusInfo(), equalTo(Response.Status.OK));
        assertThat(response.getHeaderString(HttpHeaders.AUTHORIZATION), equalTo("Bearer cafe.babe"));
        User user = (User) response.getEntity();
        assertThat(user.getName(), equalTo("robert"));
    }

    @Test
    public void testAuthenticateFalse() throws Exception {
        // Given
        when(userService.authenticate(eq("robert"), eq("bobby123"))).thenReturn(false);
        // When
        Response response = userResource.authenticate("bobby123");
        // Then
        assertThat(response.getStatusInfo(), equalTo(Response.Status.UNAUTHORIZED));
        assertThat(response.getHeaderString(HttpHeaders.AUTHORIZATION), nullValue());
        User user = (User) response.getEntity();
        assertThat(user, nullValue());
    }
}
