package com.github.gquintana.kafka.brod;

import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.net.URISyntaxException;

public class NettyJerseyServer extends  JerseyServer{
    private io.netty.channel.Channel channel;

    public NettyJerseyServer(String baseUri, ResourceConfig resourceConfig) {
        super(baseUri, resourceConfig);
    }

    public void run() throws InterruptedException, URISyntaxException {
        channel = NettyHttpContainerProvider.createServer(new URI(baseUri), resourceConfig, false);

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));

        Thread.currentThread().join();
    }

    public void close() {
        if (channel != null) {
            channel.close();
        }
    }


}
