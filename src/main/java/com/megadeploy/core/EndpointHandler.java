package com.megadeploy.core;

import com.megadeploy.annotations.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.megadeploy.utility.LogUtil.logApp;

public class EndpointHandler {

    final Map<String, Method> getEndpoints = new HashMap<>();
    final Map<String, Method> postEndpoints = new HashMap<>();
    final Map<String, Method> putEndpoints = new HashMap<>();
    final Map<String, Method> deleteEndpoints = new HashMap<>();

    public void registerEndpoints(Object endpointInstance) {
        Class<?> clazz = endpointInstance.getClass();
        if (clazz.isAnnotationPresent(Endpoint.class)) {
            Endpoint endpoint = clazz.getAnnotation(Endpoint.class);
            String basePath = endpoint.value().endsWith("/") ? endpoint.value().substring(0, endpoint.value().length() - 1) : endpoint.value();

            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Get.class)) {
                    Get get = method.getAnnotation(Get.class);
                    String path = basePath + (get.value().startsWith("/") ? get.value() : "/" + get.value());
                    path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
                    getEndpoints.put(path, method);
                    logApp("Registered GET endpoint: " + path);
                }
                if (method.isAnnotationPresent(Post.class)) {
                    Post post = method.getAnnotation(Post.class);
                    String path = basePath + (post.value().startsWith("/") ? post.value() : "/" + post.value());
                    path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
                    postEndpoints.put(path, method);
                    logApp("Registered POST endpoint: " + path);
                }
                if (method.isAnnotationPresent(Put.class)) {
                    Put put = method.getAnnotation(Put.class);
                    String path = basePath + (put.value().startsWith("/") ? put.value() : "/" + put.value());
                    path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
                    putEndpoints.put(path, method);
                    logApp("Registered PUT endpoint: " + path);
                }
                if (method.isAnnotationPresent(Delete.class)) {
                    Delete delete = method.getAnnotation(Delete.class);
                    String path = basePath + (delete.value().startsWith("/") ? delete.value() : "/" + delete.value());
                    path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
                    deleteEndpoints.put(path, method);
                    logApp("Registered DELETE endpoint: " + path);
                }
            }
        }
    }

    public Method getGetEndpoint(String path) {
        return getEndpoints.get(path);
    }

    public Method getPostEndpoint(String path) {
        return postEndpoints.get(path);
    }

    public Method getPutEndpoint(String path) {
        return putEndpoints.get(path);
    }

    public Method getDeleteEndpoint(String path) {
        return deleteEndpoints.get(path);
    }
}