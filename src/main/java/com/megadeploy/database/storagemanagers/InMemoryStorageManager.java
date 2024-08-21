package com.megadeploy.database.storagemanagers;

import com.megadeploy.annotations.core.DataObject;
import com.megadeploy.database.interfaces.StorageManagerVoid;
import com.megadeploy.utility.LogUtil;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InMemoryStorageManager implements StorageManagerVoid {

    private final Connection connection;

    public InMemoryStorageManager(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    // Create table based on DataObject annotation
    @Override
    public void createTable(Class<?> clazz) throws SQLException {
        if (!clazz.isAnnotationPresent(DataObject.class)) {
            throw new IllegalArgumentException("Class is not annotated with @DataObject");
        }

        DataObject dataObject = clazz.getAnnotation(DataObject.class);
        String tableName = dataObject.tableName().isEmpty()? clazz.getSimpleName() : dataObject.tableName();

        // Generate table definition based on class fields
        StringBuilder tableDefinition = new StringBuilder();
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.getName().equals("id")) { // Skip the 'id' field
                tableDefinition.append(field.getName()).append(" VARCHAR(255),");
            }
        }
        tableDefinition.append("id VARCHAR(36) PRIMARY KEY"); // Add the 'id' field as primary key

        String sql = "CREATE TABLE " + tableName + " (" + tableDefinition.toString() + ")";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
        }
    }


    @Override
    public void save(Object entity) throws SQLException, IllegalAccessException, NoSuchFieldException {
        Class<?> clazz = entity.getClass();
        if (!clazz.isAnnotationPresent(DataObject.class)) {
            throw new IllegalArgumentException("Class is not annotated with @DataObject");
        }

        DataObject dataObject = clazz.getAnnotation(DataObject.class);
        String tableName = dataObject.tableName().isEmpty()? clazz.getSimpleName() : dataObject.tableName();

        // Generate SQL for insert
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (!field.getName().equals("id")) { // Skip the 'id' field
                columns.append(field.getName()).append(",");
                values.append("'").append(field.get(entity)).append("',");
            }
        }
        String uuid = UUID.randomUUID().toString();
        LogUtil.logApp("User UI "+uuid+" Generated");

        columns.append("id");
        values.append("'").append(uuid).append("'"); // Generate a random 'id' value

        String sql = "INSERT INTO " + tableName + " (" + columns.toString() + ") VALUES (" + values.toString() + ")";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    public <T> T load(Class<T> clazz, Object id) throws SQLException, InstantiationException, IllegalAccessException {
        if (!clazz.isAnnotationPresent(DataObject.class)) {
            throw new IllegalArgumentException("Class is not annotated with @DataObject");
        }

        DataObject dataObject = clazz.getAnnotation(DataObject.class);
        String tableName = dataObject.tableName().isEmpty() ? clazz.getSimpleName() : dataObject.tableName();

        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                T instance = clazz.newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    field.set(instance, rs.getObject(field.getName()));
                }
                return instance;
            }
        }
        return null;
    }

    @Override
    public void update(Object entity) throws SQLException, IllegalAccessException, NoSuchFieldException {
        Class<?> clazz = entity.getClass();
        if (!clazz.isAnnotationPresent(DataObject.class)) {
            throw new IllegalArgumentException("Class is not annotated with @DataObject");
        }

        DataObject dataObject = clazz.getAnnotation(DataObject.class);
        String tableName = dataObject.tableName().isEmpty() ? clazz.getSimpleName() : dataObject.tableName();

        // Generate SQL for update
        StringBuilder setClause = new StringBuilder();
        Field idField = clazz.getDeclaredField("id");
        idField.setAccessible(true);
        Object idValue = idField.get(entity);

        if (idValue == null) {
            throw new IllegalArgumentException("Entity does not have an id");
        }

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (!field.getName().equals("id")) {
                setClause.append(field.getName()).append(" = '").append(field.get(entity)).append("',");
            }
        }
        setClause.append("id = '").append(idValue).append("'");

        String sql = "UPDATE " + tableName + " SET " + setClause.toString().replaceAll(",$", "") +
                " WHERE id = '" + idValue + "'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }


    @Override
    public void delete(Class<?> clazz, Object idValue) throws SQLException {
        if (!clazz.isAnnotationPresent(DataObject.class)) {
            throw new IllegalArgumentException("Class is not annotated with @DataObject");
        }

        DataObject dataObject = clazz.getAnnotation(DataObject.class);
        String tableName = dataObject.tableName().isEmpty() ? clazz.getSimpleName() : dataObject.tableName();

        String sql = "DELETE FROM " + tableName + " WHERE id = '" + idValue + "'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    public <T> T get(Class<T> clazz, String id) throws SQLException {
        String tableName = getTableName(clazz);
        String sql = "SELECT * FROM " + tableName + " WHERE id =?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEntity(clazz, rs);
            } else {
                return null;
            }
        }
    }

    public <T> List<T> getAll(Class<T> clazz) throws SQLException {
        String tableName = getTableName(clazz);
        String sql = "SELECT * FROM " + tableName;
        List<T> resultList = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                resultList.add(mapResultSetToEntity(clazz, rs));
            }
        } catch (SQLException e) {
            throw new SQLException("Error retrieving data: " + e.getMessage(), e);
        }
        return resultList;
    }

    private <T> T mapResultSetToEntity(Class<T> clazz, ResultSet rs) throws SQLException {
        try {
            T entity = clazz.getDeclaredConstructor().newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String columnName = field.getName();
                Object value = rs.getObject(field.getName());
                if (value instanceof UUID) {
                    field.set(entity, value.toString());
                } else if (field.getType().equals(String.class)) {
                    field.set(entity, rs.getString(columnName));
                } else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                    field.set(entity, rs.getInt(columnName));
                }
            }
            return entity;
        } catch (Exception e) {
            throw new SQLException("Error mapping ResultSet to entity", e);
        }
    }

    private String getTableName(Class<?> clazz) {
        DataObject dataObject = clazz.getAnnotation(DataObject.class);
        return dataObject != null && !dataObject.tableName().isEmpty() ?
                dataObject.tableName() : clazz.getSimpleName();
    }
}
