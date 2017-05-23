package com.github.gquintana.kafka.brod;

import org.glassfish.jersey.server.ResourceConfig;

public abstract class JerseyServer {
    protected final String baseUri;
    protected final ResourceConfig resourceConfig;

    protected JerseyServer(String baseUri, ResourceConfig resourceConfig) {
        this.baseUri = baseUri;
        this.resourceConfig = resourceConfig;
    }

    public abstract void run() throws Exception;
    public abstract void close();
}
