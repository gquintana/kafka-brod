package com.github.gquintana.kafka.brod;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.gquintana.kafka.brod.broker.BrokerService;
import com.github.gquintana.kafka.brod.cache.CacheResponseFilter;
import com.github.gquintana.kafka.brod.consumer.ConsumerGroupService;
import com.github.gquintana.kafka.brod.security.BasicAuthRequestFilter;
import com.github.gquintana.kafka.brod.security.FileBasedSecurityService;
import com.github.gquintana.kafka.brod.security.SecurityService;
import com.github.gquintana.kafka.brod.topic.PartitionService;
import com.github.gquintana.kafka.brod.topic.TopicService;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ext.ContextResolver;
import java.lang.reflect.Constructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KafkaBrodApplication implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaBrodApplication.class);
    private final Configuration configuration;
    private ObjectMapper objectMapper;
    private ZookeeperService zookeeperService;
    private BrokerService brokerService;
    private TopicService topicService;
    private ResourceConfig resourceConfig;
    private JerseyServer jerseyServer;
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
            securityService = instantiate(securityServiceClass, new Class[]{Configuration.class}, new Object[]{configuration.getAsConfiguration("http.security")});
        } catch (ReflectiveOperationException e) {
            securityService = null;
        }
        brokerService = new BrokerService(zookeeperService, objectMapper, configuration.getAsInteger("kafka.connectionTimeout").orElse(1000));
        topicService = new TopicService(zookeeperService);
        partitionService = new PartitionService(zookeeperService);
        consumerGroupService = new ConsumerGroupService(configuration.getAsString("kafka.servers").get());

        swaggerConfig();
        resourceConfig();

        jerseyServer().run();
    }

    private JerseyServer jerseyServer() throws ReflectiveOperationException {
        Class<? extends JerseyServer> httpServerClass = configuration.getAsClass("http.server.class")
            .orElse(Class.forName("com.github.gquintana.kafka.brod.NettyJerseyServer"));

        jerseyServer = instantiate(httpServerClass,
            new Class[]{String.class, ResourceConfig.class},
            new Object[]{configuration.getAsString("http.server.baseUrl").get(), resourceConfig});
        return jerseyServer;
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

    private void swaggerConfig() {
        String baseUrl = configuration.getAsString("http.server.baseUrl").get();
        Matcher matcher = Pattern.compile("(https?)://([^:/]+(?::\\d+)?)(/.*)?").matcher(baseUrl);
        String resources = getClass().getPackage().getName();
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        if (matcher.matches()) {
            beanConfig.setSchemes(new String[] {matcher.group(1)});
            beanConfig.setHost(matcher.group(2));
            beanConfig.setBasePath(matcher.group(3));
        } else {
            beanConfig.setSchemes(new String[]{"http"});
            beanConfig.setHost(baseUrl);
            beanConfig.setBasePath("/");
        }
        beanConfig.setResourcePackage(resources);
        beanConfig.setTitle("Kafka Brod");
        beanConfig.setDescription("Apache Kafka monitoring and manager web based tool");
        beanConfig.setScan(true);
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
        if (configuration.getAsBoolean("http.cache.enabled").orElse(false)) {
            CacheResponseFilter filter = new CacheResponseFilter(configuration.getAsString("http.cache.control").orElse("max-age=10"));
            resourceConfig.register(filter);
        }
        resourceConfig.registerInstances(resources.applicationResource());
        resourceConfig.register(new ObjectMapperContextResolver(objectMapper));
        resourceConfig.register(LoggingFeature.class);
        // Swagger
        resourceConfig.register(ApiListingResource.class);
        resourceConfig.register(SwaggerSerializers.class);
        return resourceConfig;
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
        if (jerseyServer != null) {
            jerseyServer.close();
        }
    }

    private <T> T instantiate(Class<T> clazz, Class[] argClasses, Object[] argValues) throws ReflectiveOperationException {
        Constructor<? extends T> objectCtor = clazz.getConstructor(argClasses);
        return objectCtor.newInstance(argValues);
    }
}
