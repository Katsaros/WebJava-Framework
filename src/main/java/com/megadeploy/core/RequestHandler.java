package com.megadeploy.core;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RequestHandler extends HttpServlet {

    private final EndpointHandler endpointHandler;

    public RequestHandler(EndpointHandler endpointHandler) {
        this.endpointHandler = endpointHandler;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp, "POST");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp, "PUT");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp, "DELETE");
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp, String methodType) throws IOException {
        String path = req.getPathInfo();

        try {
            Method method = null;
            switch (methodType) {
                case "GET":
                    method = endpointHandler.getGetEndpoint(path);
                    break;
                case "POST":
                    method = endpointHandler.getPostEndpoint(path);
                    break;
                case "PUT":
                    method = endpointHandler.getPutEndpoint(path);
                    break;
                case "DELETE":
                    method = endpointHandler.getDeleteEndpoint(path);
                    break;
            }

            if (method != null) {
                method.invoke(method.getDeclaringClass().newInstance());
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}