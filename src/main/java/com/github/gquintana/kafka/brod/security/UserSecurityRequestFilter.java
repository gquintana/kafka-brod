package com.github.gquintana.kafka.brod.security;

import io.jsonwebtoken.JwtException;

import javax.annotation.Priority;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Priority(Priorities.AUTHENTICATION)
public class UserSecurityRequestFilter implements ContainerRequestFilter {
    private final String encoding;
    private final UserService userService;
    private final JwtService jwtService;

    public UserSecurityRequestFilter(String encoding, UserService userService, JwtService jwtService) {
        this.encoding = encoding;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    private static final Pattern AUTHORIZATION_PATTERN = Pattern.compile("^(\\w+) +(.*)$");
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authorization = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isEmpty()) {
            return;
        }
        Matcher authorizationMatcher = AUTHORIZATION_PATTERN.matcher(authorization);
        if (!authorizationMatcher.matches()) {
            throw new AuthenticationException("Invalid Authorization header");
        }
        String authorizationScheme = authorizationMatcher.group(1);
        String authorizationValue = authorizationMatcher.group(2);
        SecurityContext securityContext;
        switch (authorizationScheme.toLowerCase()) {
            case "basic":
                securityContext = handleBasicAuthentication(authorizationValue);
                break;
            case "bearer":
                securityContext = handleBearerAuthentication(authorizationValue);
                break;
            default:
                throw new AuthenticationException("Authentication scheme "+authorizationScheme+ " not supported");

        }
        requestContext.setSecurityContext(securityContext);
    }

    private SecurityContext handleBearerAuthentication(String jwtToken) throws AuthenticationException {
        try {
            String userName = jwtService.parseToken(jwtToken);
            return new UserSecurityContext(userService, userName);
        } catch (JwtException e) {
            throw new AuthenticationException("Invalid JWT Token");
        }
    }

    private SecurityContext handleBasicAuthentication(String value) throws UnsupportedEncodingException, AuthenticationException {
        String userPass = new String(Base64.getDecoder().decode(value), encoding);
        int sepPos = userPass.indexOf(':');
        if (sepPos < 1 || sepPos == userPass.length() - 1) {
            throw new AuthenticationException("Invalid syntax for Authorization header");
        }
        String userName = userPass.substring(0, sepPos);
        String password = userPass.substring(sepPos + 1, userPass.length());
        if (!userService.authenticate(userName, password)) {
            throw new AuthenticationException("Invalid username or password");
        }
        return new UserSecurityContext(userService, userName);
    }

}
