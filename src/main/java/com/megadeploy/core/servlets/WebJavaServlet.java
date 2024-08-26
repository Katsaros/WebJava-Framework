package com.megadeploy.core.servlets;

import com.megadeploy.annotations.core.DataObject;
import com.megadeploy.annotations.core.Operator;
import com.megadeploy.annotations.core.Storage;
import com.megadeploy.annotations.initializer.AutoInitialize;
import com.megadeploy.core.EndpointHandler;
import com.megadeploy.dependencyinjection.DependencyRegistry;
import com.megadeploy.utility.JsonResponseUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.megadeploy.utility.LogUtil.logWebJava;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.stream.Collectors;

public class WebJavaServlet extends HttpServlet {

    private final EndpointHandler endpointHandler;
    private final DependencyRegistry dependencyRegistry;

    public WebJavaServlet(EndpointHandler endpointHandler, DependencyRegistry dependencyRegistry) {
        this.endpointHandler = endpointHandler;
        this.dependencyRegistry = dependencyRegistry;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleRequest(req, resp, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleRequest(req, resp, "POST");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleRequest(req, resp, "PUT");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleRequest(req, resp, "DELETE");
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp, String methodType) throws IOException {
        String path = req.getPathInfo();
        path = (path != null && path.endsWith("/")) ? path.substring(0, path.length() - 1) : path;
        path = (path == null || path.isEmpty()) ? "/" : path;

        logWebJava("Handling request: " + methodType + " " + path);

        try {
            Method method = getEndpointMethod(methodType, path);
            if (method != null) {
                Object endpointInstance = getOrCreateEndpointInstance(method.getDeclaringClass());

                Object[] methodArguments = parseRequestBody(req, method.getParameterTypes());

                Object result = method.invoke(endpointInstance, methodArguments);
                String jsonResponse = JsonResponseUtil.toJson(result);

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                resp.getWriter().write(jsonResponse != null ? jsonResponse : "");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Internal server error: " + e.getMessage());
        }
    }

    private Object[] parseRequestBody(HttpServletRequest req, Class<?>[] parameterTypes) throws IOException {
        if (parameterTypes.length == 0) {
            return new Object[0];
        }

        if (parameterTypes.length == 1) {
            String jsonBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            return new Object[]{JsonResponseUtil.fromJson(jsonBody, parameterTypes[0])};
        }

        throw new IllegalArgumentException("Unsupported number of parameters in method: " + Arrays.toString(parameterTypes));
    }

    private Method getEndpointMethod(String methodType, String path) {
        switch (methodType) {
            case "GET":
                return endpointHandler.getGetEndpoint(path);
            case "POST":
                return endpointHandler.getPostEndpoint(path);
            case "PUT":
                return endpointHandler.getPutEndpoint(path);
            case "DELETE":
                return endpointHandler.getDeleteEndpoint(path);
            default:
                return null;
        }
    }

    private Object getOrCreateEndpointInstance(Class<?> endpointClass) throws Exception {
        Object instance = dependencyRegistry.getInstanceByType(endpointClass);
        if (instance != null) {
            return instance;
        }

        Constructor<?>[] constructors = endpointClass.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() > 0) {
                Object[] parameters = resolveDependencies(constructor.getParameterTypes());
                instance = constructor.newInstance(parameters);
                break;
            }
        }

        if (instance == null) {
            instance = endpointClass.getDeclaredConstructor().newInstance();
        }

        injectDependencies(instance);
        dependencyRegistry.register(endpointClass, instance);

        return instance;
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

    private void injectDependencies(Object instance) throws IllegalAccessException {
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(AutoInitialize.class) ||
                    field.isAnnotationPresent(Operator.class) ||
                    field.isAnnotationPresent(DataObject.class) ||
                    field.isAnnotationPresent(Storage.class)) {

                Class<?> dependencyClass = field.getType();
                Object dependencyInstance = dependencyRegistry.getInstanceByType(dependencyClass);
                if (dependencyInstance != null) {
                    field.setAccessible(true);
                    field.set(instance, dependencyInstance);
                }
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

        if (constructors.length == 1 && constructors[0].getParameterCount() == 0) {
            return clazz.getDeclaredConstructor().newInstance();
        }

        throw new NoSuchMethodException("No suitable constructor found for " + clazz.getName());
    }
}
