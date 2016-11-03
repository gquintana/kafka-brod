package com.github.gquintana.kafka.brod.security;

import com.github.gquintana.kafka.brod.Configuration;

import java.util.*;

public class FileBasedSecurityService implements SecurityService {
    private final Configuration configuration;

    public FileBasedSecurityService(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean authenticate(String userName, String password) {
        if (userName == null || password == null) {
            return false;
        }
        userName = userName.trim();
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
    public boolean hasRole(String userName, String role) {
        if (userName == null || role == null) {
            return false;
        }
        userName = userName.trim();
        Optional<String> optProfile= getProfile(userName);
        if (!optProfile.isPresent()) {
            return false;
        }
        String profile = optProfile.get().trim();
        return getRoles(profile).contains(role.trim());
    }

    private Optional<String> getProfile(String userName) {
        return configuration.getAsString("fileBased.user." + userName + ".profile");
    }

    private Set<String> getRoles(String profile) {
        Optional<String> optRoles = configuration.getAsString("fileBased.profile." + profile + ".roles");
        Set<String> roles;
        if (optRoles.isPresent()) {
            roles = new HashSet<>(Arrays.asList(optRoles.get().trim().split("\\s*,\\s*")));
        } else {
            roles = Collections.emptySet();
        }
        return roles;
    }
}
