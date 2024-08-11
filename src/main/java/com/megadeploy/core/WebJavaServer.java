package com.megadeploy.core;

import com.megadeploy.annotations.core.DataObject;
import com.megadeploy.annotations.core.Operator;
import com.megadeploy.annotations.core.Storage;
import com.megadeploy.annotations.initializer.AutoInitialize;
import com.megadeploy.annotations.operators.InMemoryDatabaseOperator;
import com.megadeploy.core.servlets.OpenApiServlet;
import com.megadeploy.core.servlets.SwaggerUiServlet;
import com.megadeploy.core.servlets.WebJavaServlet;
import com.megadeploy.database.InMemoryDatabaseInitializer;
import com.megadeploy.dependencyinjection.DependencyRegistry;
import com.megadeploy.endpoints.StatusEndpoint;
import com.megadeploy.generators.OpenApiGenerator;
import com.megadeploy.utility.BannerUtil;
import com.megadeploy.utility.LogUtil;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.List;

public class WebJavaServer {
    private final Server server;
    private final EndpointHandler endpointHandler;
    private final String basePackage;
    private final DependencyRegistry dependencyRegistry;
    private final int mainPort;
    private static final String OPENAPI_JSON = "src/main/resources/openapi.json";
    private final InMemoryDatabaseInitializer inMemoryDatabaseInitializer;

    public WebJavaServer(int port, Class<?> mainClass) {
        this.server = new Server(port);
        this.endpointHandler = new EndpointHandler();
        this.dependencyRegistry = new DependencyRegistry();
        this.basePackage = mainClass.getPackage().getName();
        this.mainPort = port;
        this.inMemoryDatabaseInitializer = new InMemoryDatabaseInitializer();
    }

    public void start() throws Exception {
        printWebJavaBanner();
        initializeDatabase();
        initializeAutoInitializeClasses();
        findAndRegisterAllEndpoints();
        generateOpenApiSpec();
        configureAndStartServer();
    }

    public void stop() throws Exception {
        shutdownInMemoryDatabase();
        server.stop();
    }

    private void shutdownInMemoryDatabase() {
        inMemoryDatabaseInitializer.shutdownDatabase();
    }

    private void initializeDatabase() {
        inMemoryDatabaseInitializer.initializeDatabase();
        if (inMemoryDatabaseInitializer.getConnection() != null) {
            Connection connection = inMemoryDatabaseInitializer.getConnection();
            dependencyRegistry.register(Connection.class, connection);
            dependencyRegistry.register(InMemoryDatabaseOperator.class, new InMemoryDatabaseOperator(connection));
        }
    }

    private void findAndRegisterAllEndpoints() throws Exception {
        LogUtil.logWebJavaN("Registering All Application Endpoints");
        registerMainFrameworkEndpoints();
        scanForEndpointsInTheApplicationBasePackageAndSubPackages();
    }

    private static void printWebJavaBanner() {
        BannerUtil.printBanner();
    }

    private void registerMainFrameworkEndpoints() {
        addEndpoint(new StatusEndpoint());
    }

    public void addEndpoint(Object endpointInstance) {
        endpointHandler.registerEndpoints(endpointInstance);
    }

    private void scanForEndpointsInTheApplicationBasePackageAndSubPackages() throws Exception {
        EndpointScanner scanner = new EndpointScanner(endpointHandler);
        scanner.scanAndRegisterEndpoints(basePackage);
    }

    private void configureAndStartServer() throws Exception {
        LogUtil.logWebJavaN("Starting Server");
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new WebJavaServlet(endpointHandler, dependencyRegistry)), "/*");

        // Register the OpenAPI servlet
        context.addServlet(new ServletHolder(new OpenApiServlet()), "/openapi.json");

        // Register the Swagger UI servlet
        context.addServlet(new ServletHolder(new SwaggerUiServlet(mainPort)), "/swagger-ui/*");

        server.start();
        server.join();
        Runtime.getRuntime().addShutdownHook(new Thread(inMemoryDatabaseInitializer::shutdownDatabase));
    }

    private void initializeAutoInitializeClasses() throws Exception {
        List<Class<?>> classes = ClassFinder.findClasses(basePackage);
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(AutoInitialize.class) ||
                    clazz.isAnnotationPresent(Operator.class) ||
                    clazz.isAnnotationPresent(DataObject.class) ||
                    clazz.isAnnotationPresent(Storage.class)) {

                Object instance = createInstanceWithDependencies(clazz);
                dependencyRegistry.register(clazz, instance);
            }
        }
    }

    private Object createInstanceWithDependencies(Class<?> clazz) throws Exception {
        Constructor<?>[] constructors = clazz.getConstructors();

        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(AutoInitialize.class)) {
                Object[] parameters = resolveDependencies(constructor.getParameterTypes());
                return constructor.newInstance(parameters);
            }
        }
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object[] resolveDependencies(Class<?>[] parameterTypes) {
        Object[] parameters = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameters[i] = dependencyRegistry.getInstanceByType(parameterTypes[i]);
        }
        return parameters;
    }

    private void generateOpenApiSpec() throws Exception {
        OpenApiGenerator.generateOpenApiSpec(OPENAPI_JSON, basePackage);
    }

    public static String getOpenapiJson() {
        return OPENAPI_JSON;
    }
}
