package com.megadeploy.annotations.operators;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InMemoryDatabaseOperator {

    protected final Connection connection;

    public InMemoryDatabaseOperator(Connection connection) {
        this.connection = connection;
    }

    public void createTable(String tableName, String tableDefinition) throws SQLException {
        String sql = "CREATE TABLE " + tableName + " (" + tableDefinition + ")";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
        }
    }

    public void insertData(String tableName, String columns, String values) throws SQLException {
        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    public void updateData(String tableName, String setClause, String whereClause) throws SQLException {
        String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + whereClause;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    public ResultSet getData(String tableName, String columns, String whereClause) throws SQLException {
        String sql = "SELECT " + columns + " FROM " + tableName + " WHERE " + whereClause;
        PreparedStatement stmt = connection.prepareStatement(sql);
        return stmt.executeQuery();
    }

    public void deleteData(String tableName, String whereClause) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE " + whereClause;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }
}

