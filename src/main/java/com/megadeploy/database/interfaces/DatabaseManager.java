package com.megadeploy.database.interfaces;

import java.sql.SQLException;

public interface DatabaseManager {
    void createTable(Class<?> clazz) throws SQLException;
    void save(Object entity) throws SQLException, IllegalAccessException, NoSuchFieldException;
    void update(Object entity) throws SQLException, IllegalAccessException, NoSuchFieldException;
    void delete(Class<?> clazz, Object id) throws SQLException;
}
