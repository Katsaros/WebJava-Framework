package com.megadeploy.core;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.megadeploy.utility.LogUtil.logWebJava;

public class WebJavaServlet extends HttpServlet {

    private final EndpointHandler endpointHandler;

    public WebJavaServlet(EndpointHandler endpointHandler) {
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

        if (path != null) {
            path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        }
        if (path == null || path.isEmpty()) {
            path = "/";
        }

        logWebJava("Handling request: " + methodType + " " + path);

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
                Object endpointInstance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
                String result = (String) method.invoke(endpointInstance);
                if (result == null) {
                    result = "";
                }
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(result);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}