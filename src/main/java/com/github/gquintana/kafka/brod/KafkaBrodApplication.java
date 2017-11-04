package com.github.gquintana.kafka.brod;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.gquintana.kafka.brod.broker.BrokerJmxService;
import com.github.gquintana.kafka.brod.broker.BrokerService;
import com.github.gquintana.kafka.brod.cache.CacheControlResponseFilter;
import com.github.gquintana.kafka.brod.consumer.ConsumerGroupService;
import com.github.gquintana.kafka.brod.consumer.ConsumerJmxService;
import com.github.gquintana.kafka.brod.jmx.JmxConfiguration;
import com.github.gquintana.kafka.brod.jmx.JmxService;
import com.github.gquintana.kafka.brod.security.*;
import com.github.gquintana.kafka.brod.topic.PartitionJmxService;
import com.github.gquintana.kafka.brod.topic.PartitionService;
import com.github.gquintana.kafka.brod.topic.TopicService;
import io.jsonwebtoken.SignatureAlgorithm;
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toMap;

public class KafkaBrodApplication implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaBrodApplication.class);
    private final Configuration configuration;
    private ObjectMapper objectMapper;
    private ZookeeperService zookeeperService;
    private KafkaService kafkaService;
    private BrokerService brokerService;
    private TopicService topicService;
    private ResourceConfig resourceConfig;
    private JerseyServer jerseyServer;
    private PartitionService partitionService;
    private ConsumerGroupService consumerGroupService;
    private UserService userService;
    private JmxService jmxService;
    private BrokerJmxService brokerJmxService;
    private PartitionJmxService partitionJmxService;
    private JwtService jwtService;
    private ConsumerJmxService consumerJmxService;

    public KafkaBrodApplication(Configuration configuration) {
        this.configuration = configuration;
    }

    public void run() throws Exception {
        zookeeperService = new ZookeeperService(
            configuration.getAsString("zookeeper.servers").get(),
            configuration.getAsInteger("zookeeper.sessionTimeout").get(),
            configuration.getAsInteger("zookeeper.connectionTimeout").get());
        kafkaService = new KafkaService(
            configuration.getAsString("kafka.servers").get(),
            configuration.getAsString("kafka.clientId").orElse("kafka-brod"));
        jmxService = new JmxService();
        jwtService = new JwtService(
            configuration.getAsString("http.security.jwt.issuer").orElse("kafka-brod"),
            configuration.getAsString("http.security.jwt.signatureAlgorithm").map(String::toUpperCase).map(SignatureAlgorithm::forName).orElse(SignatureAlgorithm.HS256),
            null
        );

        objectMapper();

        try {
            Class<? extends UserService> securityServiceClass = configuration.getAsClass("http.security.service.class")
                .orElse(FileBasedUserService.class);
            userService = instantiate(securityServiceClass, new Class[]{Configuration.class}, new Object[]{configuration.getAsConfiguration("http.security")});
        } catch (ReflectiveOperationException e) {
            userService = null;
        }
        brokerService = new BrokerService(zookeeperService, objectMapper, configuration.getAsInteger("kafka.connectionTimeout").orElse(1000), kafkaService);
        JmxConfiguration brokerJmxConfiguration = JmxConfiguration.create(configuration, "kafka");
        brokerJmxService = new BrokerJmxService(jmxService, brokerJmxConfiguration);
        topicService = new TopicService(zookeeperService);
        partitionService = new PartitionService(zookeeperService, kafkaService);
        partitionJmxService = new PartitionJmxService(jmxService, () -> brokerService.getControllerBroker().orElse(null), brokerJmxConfiguration);
        consumerGroupService = new ConsumerGroupService(kafkaService);
        consumerJmxService = new ConsumerJmxService(jmxService, consumerJmxConfigurations());

        swaggerConfig();
        resourceConfig();

        jerseyServer().run();
    }

    private Map<String, JmxConfiguration> consumerJmxConfigurations() {
        final String keyPrefix = "consumer";
        final String keySuffix = ".jmx.port";
        Configuration consumerConfig = configuration.getAsConfiguration(keyPrefix);
        return consumerConfig.getAsMap().keySet().stream()
            .filter(key -> key.endsWith(keySuffix))
            .map(key -> key.substring(0, key.length() - keySuffix.length()))
            .collect(toMap(groupId -> groupId,
                groupId -> JmxConfiguration.create(configuration, keyPrefix + "." + groupId)));
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

    public BrokerJmxService brokerJmxService() {
        return brokerJmxService;
    }

    public PartitionJmxService partitionJmxService() {
        return partitionJmxService;
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

        if (configuration.getAsBoolean("http.security.enabled").orElse(false) && userService != null) {
            LOGGER.info("Security enabled with service {}", userService.getClass().getSimpleName());
            UserSecurityRequestFilter filter = new UserSecurityRequestFilter("UTF-8", userService, jwtService);
            resourceConfig.register(filter);
            resourceConfig.register(RolesAllowedDynamicFeature.class);
        }

        if (configuration.getAsBoolean("http.security.cors.enabled").orElse(false)) {
            resourceConfig.register(CorsResponseFilter.class);
        }

        resourceConfig.register(RuntimeExceptionMapper.class);
        if (configuration.getAsBoolean("http.cache.enabled").orElse(false)) {
            CacheControlResponseFilter filter = new CacheControlResponseFilter(configuration.getAsString("http.cache.control").orElse("max-age=10"));
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

    public ConsumerJmxService consumerJmxService() {
        return consumerJmxService;
    }

    public UserService userService() {
        return userService;
    }

    public JwtService jwtService() {
        return jwtService;
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
