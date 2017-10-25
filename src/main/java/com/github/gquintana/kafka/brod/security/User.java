package com.github.gquintana.kafka.brod.security;

import java.util.Set;

public class User {
    private String name;
    private Set<String> roles;

    public User(String name, Set<String> roles) {
        this.name = name;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
