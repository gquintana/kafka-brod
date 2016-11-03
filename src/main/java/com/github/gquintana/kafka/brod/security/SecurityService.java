package com.github.gquintana.kafka.brod.security;

public interface SecurityService {
    boolean authenticate(String userName, String password);

    boolean hasRole(String userName, String role);
}
