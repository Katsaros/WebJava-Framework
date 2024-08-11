package com.megadeploy.annotations.operators;

import com.megadeploy.storages.InMemoryStorage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InMemoryDatabaseOperator {

    private InMemoryStorage inMemoryStorage;

    public InMemoryDatabaseOperator(InMemoryStorage inMemoryStorage) {
        this.inMemoryStorage = inMemoryStorage;
    }

    public void createTable(String tableName, String tableDefinition) throws SQLException {
        inMemoryStorage.createTable(tableName, tableDefinition);
    }

    public void insertData(String tableName, String columns, String values) throws SQLException {
        inMemoryStorage.insertData(tableName, columns, values);
    }

    public void updateData(String tableName, String setClause, String whereClause) throws SQLException {
        inMemoryStorage.updateData(tableName, setClause, whereClause);
    }

    public ResultSet getData(String tableName, String columns, String whereClause) throws SQLException {
        return inMemoryStorage.getData(tableName, columns, whereClause);
    }

    public void deleteData(String tableName, String whereClause) throws SQLException {
        inMemoryStorage.deleteData(tableName, whereClause);
    }
}
