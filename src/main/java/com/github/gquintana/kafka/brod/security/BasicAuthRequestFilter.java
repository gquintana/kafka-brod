package com.github.gquintana.kafka.brod.security;

import javax.annotation.Priority;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.Base64;

@Priority(1000)
public class BasicAuthRequestFilter implements ContainerRequestFilter {
    private final String encoding;
    private final SecurityService securityService;

    public BasicAuthRequestFilter(String encoding, SecurityService securityService) {
        this.encoding = encoding;
        this.securityService = securityService;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authorization = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isEmpty()) {
            throw new AuthenticationException("Authentication credentials are required");
        }
        if (!authorization.toLowerCase().startsWith("basic ")) {
            throw new AuthenticationException("Only basic authentication is supported");
        }
        authorization = authorization.substring("Basic ".length()).trim();
        String userPass = new String(Base64.getDecoder().decode(authorization), encoding);
        int sepPos = userPass.indexOf(':');
        if (sepPos < 1 || sepPos == userPass.length() - 1) {
            throw new AuthenticationException("Invalid syntax for Authorization header");
        }
        String userName = userPass.substring(0, sepPos);
        String password = userPass.substring(sepPos + 1, userPass.length());
        if (!securityService.authenticate(userName, password)) {
            throw new AuthenticationException("Invalid username or password");
        }
        requestContext.setSecurityContext(new BasicAuthSecurityContext(securityService, userName));
    }

}
