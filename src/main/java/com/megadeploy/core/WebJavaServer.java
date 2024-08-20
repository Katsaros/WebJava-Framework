package com.megadeploy.core;

import com.megadeploy.annotations.core.DataObject;
import com.megadeploy.annotations.core.Endpoint;
import com.megadeploy.annotations.core.Operator;
import com.megadeploy.annotations.core.Storage;
import com.megadeploy.annotations.initializer.AutoInitialize;
import com.megadeploy.core.scanners.ClassFinder;
import com.megadeploy.database.interfaces.DatabaseManager;
import com.megadeploy.database.storagemanagers.InMemoryStorageManager;
import com.megadeploy.core.servlets.OpenApiServlet;
import com.megadeploy.core.servlets.SwaggerUiServlet;
import com.megadeploy.core.servlets.WebJavaServlet;
import com.megadeploy.database.initializers.InMemoryDatabaseInitializer;
import com.megadeploy.dependencyinjection.DependencyRegistry;
import com.megadeploy.endpoints.StatusEndpoint;
import com.megadeploy.generators.OpenApiGenerator;
import com.megadeploy.utility.BannerUtil;
import com.megadeploy.utility.LogUtil;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class WebJavaServer {
    private final Server server;
    private final EndpointHandler endpointHandler;
    private final DependencyRegistry dependencyRegistry;
    private final int mainPort;
    private static String appBasePackage = "";
    private static final String OPENAPI_JSON = "src/main/resources/openapi.json";
    private final InMemoryStorageManager inMemoryStorageManager;
    private InMemoryDatabaseInitializer databaseInitializer;

    public WebJavaServer(int port, Class<?> mainClass) throws SQLException, IOException, ClassNotFoundException {
        printWebJavaBanner();
        this.server = new Server(port);
        this.endpointHandler = new EndpointHandler();
        this.dependencyRegistry = new DependencyRegistry();
        this.appBasePackage = mainClass.getPackage().getName();
        this.mainPort = port;
        databaseInitializer = new InMemoryDatabaseInitializer();
        databaseInitializer.initializeDatabase();
        this.inMemoryStorageManager = new InMemoryStorageManager(databaseInitializer.getConnection());
    }

    public void start() throws Exception {
        initializeDatabase();
        findAndRegisterFrameworkEndpoints();
        initializeAutoInitializeClasses();
        generateOpenApiSpec();
        configureAndStartServer();
    }

    public void stop() throws Exception {
        server.stop();
    }

    private Connection createConnection() throws SQLException, IOException, ClassNotFoundException {
        InMemoryDatabaseInitializer initializer = new InMemoryDatabaseInitializer();
        initializer.initializeDatabase();
        return initializer.getConnection();
    }


    private void initializeDatabase() throws SQLException, IOException, ClassNotFoundException {
        DatabaseManager databaseManager = databaseInitializer.getDatabaseManager();
        dependencyRegistry.register(DatabaseManager.class, databaseManager);

        Connection connection = inMemoryStorageManager.getConnection();
        if (connection != null) {
            dependencyRegistry.register(Connection.class, connection);
            dependencyRegistry.register(InMemoryStorageManager.class, new InMemoryStorageManager(connection));
        }
    }


    private void findAndRegisterFrameworkEndpoints() throws Exception {
        LogUtil.logWebJavaN("Registering All Framework Endpoints");
        registerMainFrameworkEndpoints();
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
    }

    private void initializeAutoInitializeClasses() throws Exception {
        // Find all classes in both the framework and the demo app
        List<Class<?>> classes = ClassFinder.findClasses(appBasePackage);

        // Ensure @Storage classes are initialized first
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Storage.class)) {
                Object instance = createInstanceWithDependencies(clazz);
                dependencyRegistry.register(clazz, instance);
            }
        }

        // Initialize other annotated classes
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(AutoInitialize.class) ||
                        clazz.isAnnotationPresent(Operator.class) ||
                        clazz.isAnnotationPresent(DataObject.class)) {
                    Object instance = createInstanceWithDependencies(clazz);
                    dependencyRegistry.register(clazz, instance);
            }
        }

        LogUtil.logWebJavaN("Registering All Application Endpoints");
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Endpoint.class)) {
                Object instance = createInstanceWithDependencies(clazz);
                addEndpoint(instance);
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

        // Fallback to default constructor if no annotated constructor is found
        if (constructors.length == 1 && constructors[0].getParameterCount() == 0) {
            return clazz.getDeclaredConstructor().newInstance();
        }

        throw new NoSuchMethodException("No suitable constructor found for " + clazz.getName());
    }

    private Object[] resolveDependencies(Class<?>[] parameterTypes) {
        Object[] parameters = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameters[i] = dependencyRegistry.getInstanceByType(parameterTypes[i]);
            if (parameters[i] == null) {
                try {
                    parameters[i] = createInstanceWithDependencies(parameterTypes[i]);
                    dependencyRegistry.register(parameterTypes[i], parameters[i]);
                } catch (Exception e) {
                    throw new IllegalStateException("No instance found for type: " + parameterTypes[i], e);
                }
            }
        }
        return parameters;
    }


    private void generateOpenApiSpec() throws Exception {
        OpenApiGenerator.generateOpenApiSpec(OPENAPI_JSON, appBasePackage);
    }

    public static String getOpenapiJson() {
        return OPENAPI_JSON;
    }

    public static String getAppBasePackage() {
        return appBasePackage;
    }
}
