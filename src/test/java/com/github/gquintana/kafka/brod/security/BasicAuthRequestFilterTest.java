package com.github.gquintana.kafka.brod.security;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BasicAuthRequestFilterTest {
    @Test
    public void testMissingAuthorizationHeader() throws Exception {
        // Given
        BasicAuthRequestFilter filter = new BasicAuthRequestFilter("UTF-8", null);
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        // When
        try {
            filter.filter(request);
            fail("Exception expected");
        } catch (Exception e) {
        }
    }

    @Test
    public void testNotBasicAuthorizationHeader() throws Exception {
        // Given
        BasicAuthRequestFilter filter = new BasicAuthRequestFilter("UTF-8", null);
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
        BasicAuthRequestFilter filter = new BasicAuthRequestFilter("UTF-8", null);
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
        FileBasedSecurityService securityService = mock(FileBasedSecurityService.class);
        BasicAuthRequestFilter filter = new BasicAuthRequestFilter("UTF-8", securityService);
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
        FileBasedSecurityService securityService = mock(FileBasedSecurityService.class);
        BasicAuthRequestFilter filter = new BasicAuthRequestFilter("UTF-8", securityService);
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
}
