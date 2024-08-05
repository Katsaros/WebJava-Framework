package com.megadeploy.core;

import com.megadeploy.endpoints.StatusEndpoint;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class WebJavaServer {
    private final Server server;
    private final EndpointHandler endpointHandler;
    private final String basePackage;

    public WebJavaServer(int port, Class<?> mainClass) {
        this.server = new Server(port);
        this.endpointHandler = new EndpointHandler();
        this.basePackage = mainClass.getPackage().getName();
    }

    public void start() throws Exception {
        addEndpoint(new StatusEndpoint());

        scanForEndpointsInTheBasePackageAndSubPackages();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new WebJavaServlet(endpointHandler)), "/*");

        server.start();
        server.join();
    }

    public void addEndpoint(Object endpointInstance) {
        endpointHandler.registerEndpoints(endpointInstance);
    }

    private void scanForEndpointsInTheBasePackageAndSubPackages() throws Exception {
        EndpointScanner scanner = new EndpointScanner(endpointHandler);
        scanner.scanAndRegisterEndpoints(basePackage);
    }
}