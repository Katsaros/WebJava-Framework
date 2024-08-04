package com.megadeploy.core;

import com.megadeploy.annotations.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class EndpointHandler {

    private final Map<String, Method> getEndpoints = new HashMap<>();
    private final Map<String, Method> postEndpoints = new HashMap<>();
    private final Map<String, Method> putEndpoints = new HashMap<>();
    private final Map<String, Method> deleteEndpoints = new HashMap<>();

    public void registerEndpoints(Object endpointInstance) {
        Class<?> clazz = endpointInstance.getClass();

        if (clazz.isAnnotationPresent(Endpoint.class)) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Get.class)) {
                    Get get = method.getAnnotation(Get.class);
                    getEndpoints.put(get.value(), method);
                }
                if (method.isAnnotationPresent(Post.class)) {
                    Post post = method.getAnnotation(Post.class);
                    postEndpoints.put(post.value(), method);
                }
                if (method.isAnnotationPresent(Put.class)) {
                    Put put = method.getAnnotation(Put.class);
                    putEndpoints.put(put.value(), method);
                }
                if (method.isAnnotationPresent(Delete.class)) {
                    Delete delete = method.getAnnotation(Delete.class);
                    deleteEndpoints.put(delete.value(), method);
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