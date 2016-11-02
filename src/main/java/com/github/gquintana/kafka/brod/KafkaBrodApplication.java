package com.github.gquintana.kafka.brod;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.gquintana.kafka.brod.security.BasicAuthRequestFilter;
import com.github.gquintana.kafka.brod.security.FileBasedSecurityService;
import com.github.gquintana.kafka.brod.security.SecurityService;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.ws.rs.ext.ContextResolver;
import java.lang.reflect.Constructor;
import java.net.URI;

public class KafkaBrodApplication implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaBrodApplication.class);
    private final Configuration configuration;
    private ObjectMapper objectMapper;
    private ZookeeperService zookeeperService;
    private BrokerService brokerService;
    private TopicService topicService;
    private ResourceConfig resourceConfig;
    private HttpServer httpServer;
    private PartitionService partitionService;
    private ConsumerGroupService consumerGroupService;
    private SecurityService securityService;

    public KafkaBrodApplication(Configuration configuration) {
        this.configuration = configuration;
    }

    public void run() throws Exception {
        zookeeperService = new ZookeeperService(
            configuration.getAsString("zookeeper.servers").get(),
            configuration.getAsInteger("zookeeper.sessionTimeout").get(),
            configuration.getAsInteger("zookeeper.connectionTimeout").get());

        objectMapper();

        try {
            Class<? extends SecurityService> securityServiceClass = configuration.getAsClass("http.security.service.class")
                .orElse(FileBasedSecurityService.class);
            Constructor<? extends SecurityService> securityServiceCtor = securityServiceClass.getConstructor(Configuration.class);
            securityService = securityServiceCtor.newInstance(configuration.getAsConfiguration("http.security"));
        } catch (ReflectiveOperationException e) {
            securityService = null;
        }
        brokerService = new BrokerService(zookeeperService, objectMapper);
        topicService = new TopicService(zookeeperService);
        partitionService = new PartitionService(zookeeperService);
        consumerGroupService = new ConsumerGroupService(configuration.getAsString("kafka.servers").get());

        resourceConfig();

        httpServer(configuration.getAsString("http.baseUrl").get());
    }

    private ObjectMapper objectMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, configuration.getAsBoolean("http.json.pretty").orElse(false));
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

        if (configuration.getAsBoolean("http.security.basicAuth.enabled").orElse(false) && securityService != null) {
            LOGGER.info("Basic Auth security enabled with service {}", securityService.getClass().getSimpleName());
            BasicAuthRequestFilter filter = new BasicAuthRequestFilter("UTF-8", securityService);
            resourceConfig.register(filter);
            resourceConfig.register(RolesAllowedDynamicFeature.class);
        }

        resourceConfig.register(RuntimeExceptionMapper.class);
        resourceConfig.registerInstances(
                resources.brokersResource(),
                resources.topicsResource(),
                resources.consumerGroupsResource());
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

    public ConsumerGroupService consumerGroupService() {
        return consumerGroupService;
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
