package com.github.gquintana.kafka.brod.security;

import com.github.gquintana.kafka.brod.Configuration;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class FileBasedUserServiceTest {

    private Configuration getConfiguration() throws IOException {
        Configuration configuration = new Configuration();
        configuration.load(getClass().getResource("fileBased.properties"));
        return configuration;
    }

    private FileBasedUserService getService() throws IOException {
        return new FileBasedUserService(getConfiguration());
    }

    @Test
    public void testAuthenticate() throws Exception {
        // Given
        FileBasedUserService service = getService();
        // When Then
        assertThat(service.authenticate("admin", "admin"), is(true));
        assertThat(service.authenticate("admin", "fail"), is(false));
        assertThat(service.authenticate("user", "user"), is(true));
        assertThat(service.authenticate("user", "fail"), is(false));
        assertThat(service.authenticate("other", "other"), is(false));
    }

    @Test
    public void testHasRole() throws Exception {
        // Given
        FileBasedUserService service = getService();
        // When Then
        assertThat(service.hasRole("admin", "admin"), is(true));
        assertThat(service.hasRole("admin", "user"), is(true));
        assertThat(service.hasRole("admin", "other"), is(false));
        assertThat(service.hasRole("user", "user"), is(true));
        assertThat(service.hasRole("user", "admin"), is(false));
        assertThat(service.hasRole("other", "other"), is(false));
    }

    @Test
    public void testGetUser() throws Exception {
        // Given
        FileBasedUserService service = getService();
        // When Then
        Optional<User> admin = service.getUser("admin");
        assertThat(admin.isPresent(), is(true));
        assertThat(admin.get().getName(), equalTo("admin"));
        assertThat(admin.get().getRoles(), hasItems("ADMIN", "USER"));
        Optional<User> user = service.getUser("user");
        assertThat(user.isPresent(), is(true));
        assertThat(user.get().getName(), equalTo("user"));
        assertThat(user.get().getRoles(), hasItems("USER"));
        Optional<User> other = service.getUser("other");
        assertThat(other.isPresent(), is(false));
    }
}
