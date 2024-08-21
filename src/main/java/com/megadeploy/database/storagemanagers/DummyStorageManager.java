package com.megadeploy.database.storagemanagers;

import com.megadeploy.annotations.core.DataObject;
import com.megadeploy.database.interfaces.StorageManager;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.megadeploy.utility.LogUtil.logApp;

public class DummyStorageManager implements StorageManager {

    public DummyStorageManager() {
    }

    public void createTable(Class<?> clazz) throws SQLException {
        if (!clazz.isAnnotationPresent(DataObject.class)) {
            throw new IllegalArgumentException("Class is not annotated with @DataObject");
        }

        logApp("Creating table for class: " + clazz.getName());

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            logApp("Column: " + field.getName() + " Type: " + field.getType().getName());
        }

        logApp("Table created successfully!");
    }

    public <T> T save(T entity) throws SQLException, IllegalAccessException, NoSuchFieldException {
        logApp("Saving entity: " + entity.getClass().getName());

        Field[] fields = entity.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            logApp("Column: " + field.getName() + " Value: " + field.get(entity));
        }

        logApp("Entity saved successfully!");
        return entity;
    }

    public <T> T update(T entity) throws SQLException, IllegalAccessException, NoSuchFieldException {
        logApp("Updating entity: " + entity.getClass().getName());

        Field[] fields = entity.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            logApp("Column: " + field.getName() + " Value: " + field.get(entity));
        }

        logApp("Entity updated successfully!");
        return entity;
    }

    public <T> T delete(Class<T> clazz, Object id) throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        logApp("Deleting entity: " + clazz.getName() + " with ID: " + id);

        T entity = clazz.newInstance();
        Field idField = clazz.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id); // Set the ID for the deleted entity

        logApp("Entity deleted successfully!");
        return entity;
    }

    public <T> T get(Class<T> clazz, Object id) throws SQLException, InstantiationException, IllegalAccessException {
        logApp("Getting entity: " + clazz.getName() + " with ID: " + id);

        T entity = clazz.newInstance();

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName().equals("id")) {
                field.set(entity, id);
            } else {
                field.set(entity, "dummy value");
            }
        }

        logApp("Entity retrieved successfully!");

        return entity;
    }

    public <T> List<T> getAll(Class<T> clazz) throws SQLException, InstantiationException, IllegalAccessException {
        logApp("Getting all entities: " + clazz.getName());

        List<T> entities = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            T entity = clazz.newInstance();

            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                if (field.getName().equals("id")) {
                    field.set(entity, i);
                } else {
                    field.set(entity, "dummy value " + i);
                }
            }

            entities.add(entity);
        }

        logApp("Entities retrieved successfully!");

        return entities;
    }
}
