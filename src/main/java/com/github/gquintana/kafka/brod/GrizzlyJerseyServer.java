package com.github.gquintana.kafka.brod;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.net.URI;

public class GrizzlyJerseyServer extends JerseyServer{
    private HttpServer httpServer;

    public GrizzlyJerseyServer(String baseUri, ResourceConfig resourceConfig) {
        super(baseUri, resourceConfig);
    }

    public void run() {
        // Grizzly uses JUL
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), resourceConfig);
    }

    public void close() {
        if (httpServer != null) {
            httpServer.shutdownNow();
        }
    }
}
