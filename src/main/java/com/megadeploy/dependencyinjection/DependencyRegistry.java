package com.megadeploy.dependencyinjection;

import java.util.HashMap;
import java.util.Map;

public class DependencyRegistry {

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public <T> void register(Class<?> clazz, Object instance) {
        instances.put(clazz, instance);
    }

    public <T> T getInstance(Class<T> clazz) {
        return (T) instances.get(clazz);
    }

    public <T> T getInstanceByType(Class<T> type) {
        for (Map.Entry<Class<?>, Object> entry : instances.entrySet()) {
            if (type.isAssignableFrom(entry.getKey())) {
                return (T) entry.getValue();
            }
        }
        return null;
    }
}