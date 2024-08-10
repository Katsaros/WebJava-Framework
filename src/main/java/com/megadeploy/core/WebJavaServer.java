package com.megadeploy.core;

import com.megadeploy.annotations.core.DataObject;
import com.megadeploy.annotations.core.Operator;
import com.megadeploy.annotations.core.Storage;
import com.megadeploy.annotations.initializer.AutoInitialize;
import com.megadeploy.dependencyinjection.DependencyRegistry;
import com.megadeploy.endpoints.StatusEndpoint;
import com.megadeploy.generators.OpenApiGenerator;
import com.megadeploy.utility.BannerUtil;
import com.megadeploy.utility.LogUtil;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.List;

public class WebJavaServer {
    private final Server server;
    private final EndpointHandler endpointHandler;
    private final String basePackage;
    private final DependencyRegistry dependencyRegistry;
    private final int mainPort;
    private static final String outputPath = "src/main/resources/openapi.json";

    public WebJavaServer(int port, Class<?> mainClass) {
        this.server = new Server(port);
        this.endpointHandler = new EndpointHandler();
        this.dependencyRegistry = new DependencyRegistry();
        this.basePackage = mainClass.getPackage().getName();
        this.mainPort = port;
    }

    public void start() throws Exception {
        printWebJavaBanner();
        initializeAutoInitializeClasses();
        findAndRegisterAllEndpoints();
        generateOpenApiSpec();
        configureAndStartServer();
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
    }

    private void initializeAutoInitializeClasses() throws Exception {
        List<Class<?>> classes = ClassFinder.findClasses(basePackage);
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(AutoInitialize.class) ||
                    clazz.isAnnotationPresent(Operator.class) ||
                    clazz.isAnnotationPresent(DataObject.class) ||
                    clazz.isAnnotationPresent(Storage.class)) {

                Object instance = clazz.getDeclaredConstructor().newInstance();
                dependencyRegistry.register(clazz, instance);
            }
        }
    }

    private void generateOpenApiSpec() throws Exception {
        OpenApiGenerator.generateOpenApiSpec(outputPath, basePackage);
    }

    public static String getOutputPath() {
        return outputPath;
    }
}