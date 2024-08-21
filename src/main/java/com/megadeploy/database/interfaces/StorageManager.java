package com.megadeploy.database.interfaces;

import java.sql.SQLException;

import java.util.List;

public interface StorageManager {

    /**
     * Creates a table for the specified class based on its annotated fields.
     *
     * @param clazz the class for which the table should be created.
     * @throws SQLException if a database access error occurs.
     */
    void createTable(Class<?> clazz) throws SQLException;

    /**
     * Saves the provided entity to the storage and returns the saved entity.
     *
     * @param <T>    the type of the entity to be saved.
     * @param entity the entity to save.
     * @return the saved entity.
     * @throws SQLException           if a database access error occurs.
     * @throws IllegalAccessException if access to the entity's fields is illegal.
     * @throws NoSuchFieldException   if a field in the entity is not found.
     */
    <T> T save(T entity) throws SQLException, IllegalAccessException, NoSuchFieldException;

    /**
     * Updates the provided entity in the storage and returns the updated entity.
     *
     * @param <T>    the type of the entity to be updated.
     * @param entity the entity to update.
     * @return the updated entity.
     * @throws SQLException           if a database access error occurs.
     * @throws IllegalAccessException if access to the entity's fields is illegal.
     * @throws NoSuchFieldException   if a field in the entity is not found.
     */
    <T> T update(T entity) throws SQLException, IllegalAccessException, NoSuchFieldException;

    /**
     * Deletes the entity with the specified ID from the storage and returns the deleted entity.
     *
     * @param <T>   the type of the entity to be deleted.
     * @param clazz the class of the entity to be deleted.
     * @param id    the ID of the entity to be deleted.
     * @return the deleted entity.
     * @throws SQLException           if a database access error occurs.
     * @throws InstantiationException if the entity cannot be instantiated.
     * @throws IllegalAccessException if access to the entity's fields is illegal.
     */
    <T> T delete(Class<T> clazz, Object id) throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException;

    /**
     * Retrieves the entity with the specified ID from the storage.
     *
     * @param <T>   the type of the entity to be retrieved.
     * @param clazz the class of the entity to be retrieved.
     * @param id    the ID of the entity to be retrieved.
     * @return the retrieved entity.
     * @throws SQLException           if a database access error occurs.
     * @throws InstantiationException if the entity cannot be instantiated.
     * @throws IllegalAccessException if access to the entity's fields is illegal.
     */
    <T> T get(Class<T> clazz, Object id) throws SQLException, InstantiationException, IllegalAccessException;

    /**
     * Retrieves all entities of the specified class from the storage.
     *
     * @param <T>   the type of the entities to be retrieved.
     * @param clazz the class of the entities to be retrieved.
     * @return a list of all retrieved entities.
     * @throws SQLException           if a database access error occurs.
     * @throws InstantiationException if an entity cannot be instantiated.
     * @throws IllegalAccessException if access to an entity's fields is illegal.
     */
    <T> List<T> getAll(Class<T> clazz) throws SQLException, InstantiationException, IllegalAccessException;
}
