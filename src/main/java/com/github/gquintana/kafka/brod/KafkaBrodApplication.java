package com.github.gquintana.kafka.brod;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.ws.rs.ext.ContextResolver;
import java.net.URI;

public class KafkaBrodApplication implements AutoCloseable {
    private final Configuration configuration;
    private ObjectMapper objectMapper;
    private ZookeeperService zookeeperService;
    private BrokerService brokerService;
    private TopicService topicService;
    private ResourceConfig resourceConfig;
    private HttpServer httpServer;
    private PartitionService partitionService;

    public KafkaBrodApplication(Configuration configuration) {
        this.configuration = configuration;
    }

    public void run() throws Exception {
        zookeeperService = new ZookeeperService(
            configuration.getAsString("zookeeper.servers").get(),
            configuration.getAsInteger("zookeeper.sessionTimeout").get(),
            configuration.getAsInteger("zookeeper.connectionTimeout").get());

        objectMapper();

        brokerService = new BrokerService(zookeeperService, objectMapper);
        topicService = new TopicService(zookeeperService);
        partitionService = new PartitionService(zookeeperService);

        resourceConfig();

        httpServer(configuration.getAsString("http.baseUrl").get());
    }

    private ObjectMapper objectMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    private static class InstanceObjectResolver<T> implements ContextResolver<T> {
        private final T instance;
        public InstanceObjectResolver(T instance) {
            this.instance = instance;
        }
        @Override
        public T getContext(Class<?> aClass) {
            return instance;
        }
    }

    private static class ObjectMapperContextResolver extends InstanceObjectResolver<ObjectMapper> {
        public ObjectMapperContextResolver(ObjectMapper objectMapper) {
            super(objectMapper);
        }
    }
    private ResourceConfig resourceConfig() {
        resourceConfig = new ResourceConfig();


        Resources resources = new Resources(this);

        resourceConfig.registerInstances(
                resources.brokersResource(),
                resources.topicsResource());
        resourceConfig.register(new ObjectMapperContextResolver(objectMapper));
        resourceConfig.register(LoggingFeature.class);
        return resourceConfig;
    }

    private HttpServer httpServer(String baseUri) {
        // Grizzly uses JUL
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), resourceConfig);
        return httpServer;
    }

    public ZookeeperService zookeeperService() {
        return zookeeperService;
    }

    public BrokerService brokerService() {
        return brokerService;
    }

    public TopicService topicService() {
        return topicService;
    }

    public PartitionService partitionService() {
        return partitionService;
    }

    @Override
    public void close() throws Exception {
        if (zookeeperService != null) {
            zookeeperService.close();
        }
        if (httpServer != null) {
            httpServer.shutdownNow();
        }
    }
}
