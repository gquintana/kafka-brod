package com.github.gquintana.kafka.brod.security;

import com.github.gquintana.kafka.brod.Configuration;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FileBasedSecurityServiceTest {

    private Configuration getConfiguration() throws IOException {
        Configuration configuration = new Configuration();
        configuration.load(getClass().getResource("fileBased.properties"));
        return configuration;
    }

    private FileBasedSecurityService getService() throws IOException {
        return new FileBasedSecurityService(getConfiguration());
    }

    @Test
    public void testAuthenticate() throws Exception {
        // Given
        FileBasedSecurityService service = getService();
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
        FileBasedSecurityService service = getService();
        // When Then
        assertThat(service.hasRole("admin", "CREATE_TOPIC"), is(true));
        assertThat(service.hasRole("admin", "DELETE_TOPIC"), is(true));
        assertThat(service.hasRole("admin", "RENAME_TOPIC"), is(false));
        assertThat(service.hasRole("user", "READ_TOPIC"), is(true));
        assertThat(service.hasRole("user", "CREATE_TOPIC"), is(false));
        assertThat(service.hasRole("other", "READ_TOPIC"), is(false));
    }
}
