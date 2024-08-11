package com.megadeploy.core.servlets;

import com.megadeploy.annotations.core.DataObject;
import com.megadeploy.annotations.core.Operator;
import com.megadeploy.annotations.core.Storage;
import com.megadeploy.annotations.initializer.AutoInitialize;
import com.megadeploy.core.EndpointHandler;
import com.megadeploy.responses.ApiResponse;
import com.megadeploy.dependencyinjection.DependencyRegistry;
import com.megadeploy.utility.JsonResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.megadeploy.utility.LogUtil.logWebJava;

public class WebJavaServlet extends HttpServlet {

    private final EndpointHandler endpointHandler;
    private final DependencyRegistry dependencyRegistry;

    public WebJavaServlet(EndpointHandler endpointHandler,
                          DependencyRegistry dependencyRegistry) {
        this.endpointHandler = endpointHandler;
        this.dependencyRegistry = dependencyRegistry;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            handleRequest(req, resp, "GET");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            handleRequest(req, resp, "POST");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            handleRequest(req, resp, "PUT");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            handleRequest(req, resp, "DELETE");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp, String methodType) throws Exception {
        String path = req.getPathInfo();
        if (path != null) {
            path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        }
        if (path == null || path.isEmpty()) {
            path = "/";
        }

        logWebJava("Handling request: " + methodType + " " + path);

        String contentType = resp.getContentType();
        if (contentType != null && contentType.equals("application/json")) {
            handleApiResponseRequestType(resp);
        } else {
            handleOtherResponseRequestTypes(resp, methodType, path);
        }
    }

    private static void handleApiResponseRequestType(HttpServletResponse resp) throws Exception {
        ApiResponse<?> apiResponse = (ApiResponse<?>) resp;
        String json = apiResponse.toJson();
        resp.setContentType("application/json");
        resp.getWriter().write(json);
    }

    private void handleOtherResponseRequestTypes(HttpServletResponse resp, String methodType, String path) throws IOException {
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
                injectDependencies(endpointInstance);
                Object result = method.invoke(endpointInstance);
                String jsonResponse = JsonResponseUtil.toJson(result);
                if (jsonResponse == null) {
                    jsonResponse = "";
                }
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(jsonResponse);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void injectDependencies(Object instance) throws IllegalAccessException {
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(AutoInitialize.class) ||
                    field.isAnnotationPresent(Operator.class) ||
                    field.isAnnotationPresent(DataObject.class) ||
                    field.isAnnotationPresent(Storage.class)) {

                Class<?> dependencyClass = field.getType();
                Object dependencyInstance = dependencyRegistry.getInstanceByType(dependencyClass);
                field.setAccessible(true);
                field.set(instance, dependencyInstance);
            }
        }
    }
}