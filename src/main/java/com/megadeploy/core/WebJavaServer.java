package com.megadeploy.core;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class WebJavaServer {
    private final Server server;
    private final EndpointHandler endpointHandler;

    public WebJavaServer(int port) {
        this.server = new Server(port);
        this.endpointHandler = new EndpointHandler();
    }

    public void addEndpoint(Object endpointInstance) {
        endpointHandler.registerEndpoints(endpointInstance);
    }

    public void start() throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new WebJavaServlet(endpointHandler)), "/*");

        server.start();
        server.join();
    }
}