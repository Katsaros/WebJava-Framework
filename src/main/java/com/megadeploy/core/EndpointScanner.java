package com.megadeploy.core;

import com.megadeploy.annotations.*;

import java.lang.reflect.Method;
import java.util.List;

import static com.megadeploy.utility.UtilityTexts.logApp;

public class EndpointScanner {

    private final EndpointHandler endpointHandler;

    public EndpointScanner(EndpointHandler endpointHandler) {
        this.endpointHandler = endpointHandler;
    }

    public void scanAndRegisterEndpoints(String packageName) throws Exception {
        List<Class<?>> classes = ClassFinder.findClasses(packageName);
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Endpoint.class)) {
                registerEndpoint(clazz);
            }
        }
    }

    private void registerEndpoint(Class<?> clazz) throws Exception {
        Endpoint endpoint = clazz.getAnnotation(Endpoint.class);
        String basePath = endpoint.value().endsWith("/") ? endpoint.value().substring(0, endpoint.value().length() - 1) : endpoint.value();
        Object instance = clazz.getDeclaredConstructor().newInstance();

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Get.class)) {
                Get get = method.getAnnotation(Get.class);
                String path = basePath + (get.value().startsWith("/") ? get.value() : "/" + get.value());
                path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
                endpointHandler.getEndpoints.put(path, method);
                logApp("Registered GET endpoint: " + path);
            }
            if (method.isAnnotationPresent(Post.class)) {
                Post post = method.getAnnotation(Post.class);
                String path = basePath + (post.value().startsWith("/") ? post.value() : "/" + post.value());
                path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
                endpointHandler.postEndpoints.put(path, method);
                logApp("Registered POST endpoint: " + path);
            }
            if (method.isAnnotationPresent(Put.class)) {
                Put put = method.getAnnotation(Put.class);
                String path = basePath + (put.value().startsWith("/") ? put.value() : "/" + put.value());
                path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
                endpointHandler.putEndpoints.put(path, method);
                logApp("Registered PUT endpoint: " + path);
            }
            if (method.isAnnotationPresent(Delete.class)) {
                Delete delete = method.getAnnotation(Delete.class);
                String path = basePath + (delete.value().startsWith("/") ? delete.value() : "/" + delete.value());
                path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
                endpointHandler.deleteEndpoints.put(path, method);
                logApp("Registered DELETE endpoint: " + path);
            }
        }
    }
}
