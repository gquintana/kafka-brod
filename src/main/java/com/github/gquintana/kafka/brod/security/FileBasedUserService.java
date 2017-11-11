package com.github.gquintana.kafka.brod.security;

import com.github.gquintana.kafka.brod.Configuration;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class FileBasedUserService implements UserService {
    private final Configuration configuration;

    public FileBasedUserService(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean authenticate(String userName, String password) {
        if (userName == null || password == null) {
            return false;
        }
        Optional<String> optPassword = getPassword(userName);
        if (!optPassword.isPresent()) {
            return false;
        }
        return optPassword.get().trim().equals(password.trim());
    }

    private Optional<String> getPassword(String userName) {
        return configuration.getAsString("fileBased.user." + userName + ".password");
    }

    @Override
    public Optional<User> getUser(String userName) {
        if (userName == null || !getPassword(userName).isPresent()) {
            return Optional.empty();
        }
        Set<String> userRoles = getRoles(userName);
        return Optional.of(new User(userName, userRoles));
    }

    @Override
    public boolean hasRole(String userName, String requiredRole) {
        if (userName == null || requiredRole == null) {
            return false;
        }
        Set<String> userRoles = getRoles(userName);
        if (userRoles.isEmpty()) {
            return false;
        }
        return userRoles.contains(requiredRole.toUpperCase());
    }

    private Set<String> getRoles(String userName) {
        return configuration.getAsString("fileBased.user." + userName + ".roles")
            .map(this::parseRoles)
            .orElse(Collections.emptySet());
    }

    private Set<String> parseRoles(String roles) {
        return Stream.of(roles.split(","))
            .map(String::trim)
            .filter(r -> !r.isEmpty())
            .map(String::toUpperCase).collect(toSet());
    }
}
