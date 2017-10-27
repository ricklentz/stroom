package stroom.connectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.node.server.StroomPropertyService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Consumer;

/**
 * The generic form of a Connector Factory Service.
 *
 * A specific sub class of this would be
 * 1) Loaded as a Service
 * 2) Each Factory would be capable of creating Connectors for specific versions of the external library
 *
 * @param <C> The Connector class
 * @param <F> The Factory class, which is tied to creating instances of the Connector.
 */
public abstract class StroomAbstractConnectorFactoryService<
        C extends StroomConnector,
        F extends StroomConnectorFactory<C>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StroomAbstractConnectorFactoryService.class);

    // If a class requests a producer, but gives no name, use this value as the name
    private static final String DEFAULT_NAME = "default";

    // This property should exist with the given property prefix.
    private static final String PROP_CONNECTOR_VERSION = "connector.version";

    private final String propertyPrefix;
    private final StroomPropertyService propertyService;

    private final ExternalLibService externalLibService;
    private final Class<C> connectorClass;
    private final Class<F> factoryClass;

    // Used to keep track of the instances created, a single instance per name will be created and shared.
    private final Map<String, C> connectorsByName;

    protected StroomAbstractConnectorFactoryService(final StroomPropertyService propertyService,
                                                    final ExternalLibService externalLibService,
                                                    final String propertyPrefix,
                                                    final Class<C> connectorClass,
                                                    final Class<F> factoryClass) {
        this.propertyService = propertyService;
        this.propertyPrefix = propertyPrefix;
        this.externalLibService = externalLibService;
        this.connectorClass = connectorClass;
        this.factoryClass = factoryClass;
        this.connectorsByName = new HashMap<>();
    }

    /**
     * Overloaded version of getConnector that uses the default name.
     *
     * @param exceptionHandler Exceptions will be passed to this if calls are made to a default proxy producer.
     * @return A {@link C} configured accordingly.
     */
    public synchronized C getProducer(final Consumer<Exception> exceptionHandler) {
        return getProducer(DEFAULT_NAME, exceptionHandler);
    }

    /**
     * Users of this service can request a named Kafka connection. This is used to merge in the property names of the
     * bootstrap servers and the kafka client version.
     *
     * @param exceptionHandler Exceptions will be passed to this if calls are made to a default proxy producer.
     * @param name The named configuration of producer.
     * @return A {@link C} configured accordingly.
     */
    public synchronized C getProducer(final String name, final Consumer<Exception> exceptionHandler) {
        LOGGER.info("Retrieving the Connector Producer for " + name);

        // Create a properties shim for the named producer.
        final ConnectorProperties connectorProperties = new ConnectorPropertiesPrefixImpl(String.format(propertyPrefix, name), this.propertyService);

        // First try and find a previously created producer by this name
        C connector = connectorsByName.get(name);

        // Retrieve the settings for this named kafka
        String connectorVersion = connectorProperties.getProperty(PROP_CONNECTOR_VERSION);

        // If the named properties are empty, this is an old config
        if (null == connectorVersion) {
            connectorVersion = setupPropertyDefaults(connectorProperties);
        }

        // If a producer has not already been created for this name, it is time to create one
        if (connector == null) {
            // Loop through all the class loaders created for external JARs
            for (final ClassLoader classLoader : externalLibService.getClassLoaders()) {
                // Create a service loader for our specific factory class
                final ServiceLoader<F> serviceLoader = ServiceLoader.load(factoryClass, classLoader);
                // Iterate through the factories to see if any can create a connector for the right version.
                for (final F factory : serviceLoader) {
                    connector = factory.getConnector(connectorVersion, connectorProperties);
                    if (connector != null) {
                        break;
                    }
                }
            }
        }

        // Either store the producer, or throw an exception as one could not be found
        if (connector != null) {
            connectorsByName.put(name, connector);
        } else {
            final String msg = String.format("Could not find Stroom Connector Factory for %s: %s",
                    factoryClass.getName(),
                    connectorVersion);
            LOGGER.warn(msg);

            // Create a proxy that forwards any calls to our error handler
            final Object fallbackConnector = Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class[]{connectorClass},
                    (proxy, method, args) -> {
                        final String invokeMsg = String.format("Called method %s on default connector factory of type %s",
                                method.getName(),
                                connectorClass.getName());
                        exceptionHandler.accept(new RuntimeException(invokeMsg));
                        return null;
                    });
            connector = (C) fallbackConnector;
        }

        return connector;
    }

    public void shutdown() {
        LOGGER.info("Shutting Down Stroom Connector Producer Factory Service");
        connectorsByName.values().forEach(StroomConnector::shutdown);
        connectorsByName.clear();
    }

    protected StroomPropertyService getPropertyService() {
        return propertyService;
    }

    protected abstract String setupPropertyDefaults(final ConnectorProperties connectorProperties);
}