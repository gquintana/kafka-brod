package com.github.gquintana.kafka.brod.security;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

class BasicAuthSecurityContext implements SecurityContext {
    private final SecurityService securityService;
    private final String userName;

    public BasicAuthSecurityContext(SecurityService securityService, String userName) {
        this.securityService = securityService;
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public Principal getUserPrincipal() {
        return this::getUserName;
    }

    @Override
    public boolean isUserInRole(String role) {
        return securityService.hasRole(userName, role);
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Basic Auth";
    }
}
