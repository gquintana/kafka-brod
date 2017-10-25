package com.github.gquintana.kafka.brod.security;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

class UserSecurityContext implements SecurityContext {
    private final UserService userService;
    private final String userName;

    public UserSecurityContext(UserService userService, String userName) {
        this.userService = userService;
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
        return userService.hasRole(userName, role);
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return SecurityContext.BASIC_AUTH;
    }
}
