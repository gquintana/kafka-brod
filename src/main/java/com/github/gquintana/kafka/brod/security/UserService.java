package com.github.gquintana.kafka.brod.security;

import java.util.Optional;

public interface UserService {
    boolean authenticate(String userName, String password);

    boolean hasRole(String userName, String role);

    Optional<User> getUser(String userName);
}
