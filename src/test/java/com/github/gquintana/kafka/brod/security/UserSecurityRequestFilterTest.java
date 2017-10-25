package com.github.gquintana.kafka.brod.security;

import io.jsonwebtoken.JwtException;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UserSecurityRequestFilterTest {
    @Test
    public void testNotBasicAuthorizationHeader() throws Exception {
        // Given
        UserSecurityRequestFilter filter = new UserSecurityRequestFilter("UTF-8", null, null);
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        when(request.getHeaderString(eq("Authorization"))).thenReturn("Other abcde");
        // When
        try {
            filter.filter(request);
            fail("Exception expected");
        } catch (Exception e) {
        }
    }

    @Test
    public void testNoUserNamePasswordAuthorizationHeader() throws Exception {
        // Given
        UserSecurityRequestFilter filter = new UserSecurityRequestFilter("UTF-8", null, null);
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        when(request.getHeaderString(eq("Authorization"))).thenReturn("Basic YWJjZGU=");
        // When
        try {
            filter.filter(request);
            fail("Exception expected");
        } catch (Exception e) {
        }
    }

    @Test
    public void testAuthenticationFailed() throws Exception {
        // Given
        UserService securityService = mock(UserService.class);
        JwtService jwtService = mock(JwtService.class);
        UserSecurityRequestFilter filter = new UserSecurityRequestFilter("UTF-8", securityService, jwtService);
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        when(request.getHeaderString(eq("Authorization"))).thenReturn("Basic dXNlcjpwYXNz");
        when(securityService.authenticate(eq("user"), eq("pass"))).thenReturn(false);
        // When
        try {
            filter.filter(request);
            fail("Exception expected");
        } catch (Exception e) {
        }
        verify(securityService).authenticate(eq("user"), eq("pass"));
    }

    @Test
    public void testAuthenticationSucceeded() throws Exception {
        // Given
        UserService securityService = mock(UserService.class);
        JwtService jwtService = mock(JwtService.class);
        UserSecurityRequestFilter filter = new UserSecurityRequestFilter("UTF-8", securityService, jwtService);
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        when(request.getHeaderString(eq("Authorization"))).thenReturn("Basic dXNlcjpwYXNz");
        when(securityService.authenticate(eq("user"), eq("pass"))).thenReturn(true);
        // When
        filter.filter(request);
        // Then
        verify(securityService).authenticate(eq("user"), eq("pass"));
        ArgumentCaptor<SecurityContext> securityContextCaptor = ArgumentCaptor.forClass(SecurityContext.class);
        verify(request).setSecurityContext(securityContextCaptor.capture());
        assertNotNull(securityContextCaptor.getValue());
        assertThat(securityContextCaptor.getValue().getUserPrincipal().getName(), Matchers.equalTo("user"));

    }

    @Test
    public void testJwtTokenValid() throws Exception {
        // Given
        UserService securityService = mock(UserService.class);
        JwtService jwtService = mock(JwtService.class);
        UserSecurityRequestFilter filter = new UserSecurityRequestFilter("UTF-8", securityService, jwtService);
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        when(request.getHeaderString(eq("Authorization"))).thenReturn("Bearer token");
        when(jwtService.parseToken("token")).thenReturn("user");
        // When
        filter.filter(request);
        // Then
        ArgumentCaptor<SecurityContext> securityContextCaptor = ArgumentCaptor.forClass(SecurityContext.class);
        verify(request).setSecurityContext(securityContextCaptor.capture());
        assertNotNull(securityContextCaptor.getValue());
        assertThat(securityContextCaptor.getValue().getUserPrincipal().getName(), Matchers.equalTo("user"));
    }

    @Test
    public void testJwtTokenInvalid() throws Exception {
        // Given
        UserService securityService = mock(UserService.class);
        JwtService jwtService = mock(JwtService.class);
        UserSecurityRequestFilter filter = new UserSecurityRequestFilter("UTF-8", securityService, jwtService);
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        when(request.getHeaderString(eq("Authorization"))).thenReturn("Bearer token");
        when(jwtService.parseToken("token")).thenThrow(new JwtException("Invalid token"));
        // When
        try {
            filter.filter(request);
            fail("Exception expected");
        } catch (Exception e) {
        }
    }
}
