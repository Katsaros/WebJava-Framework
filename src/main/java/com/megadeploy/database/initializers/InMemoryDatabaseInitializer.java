package com.megadeploy.database.initializers;

import com.megadeploy.configuration.WebJavaFrameworkConfiguration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.megadeploy.utility.LogUtil.*;

import com.megadeploy.annotations.core.DataObject;
import com.megadeploy.core.WebJavaServer;
import com.megadeploy.core.scanners.ClassFinder;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.megadeploy.utility.LogUtil.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import com.megadeploy.annotations.core.DataObject;
import com.megadeploy.database.interfaces.DatabaseManager;
import com.megadeploy.database.storagemanagers.InMemoryStorageManager;

public class InMemoryDatabaseInitializer {

    private Connection connection;
    private InMemoryStorageManager inMemoryStorageManager;

    public void initializeDatabase() throws SQLException, ClassNotFoundException, IOException {
        WebJavaFrameworkConfiguration config = WebJavaFrameworkConfiguration.loadConfig();
        if (config.isEnableInMemoryDatabase()) {
            logWebJava("In-Memory Database is enabled. Initializing Apache Derby...");

            String url = "jdbc:derby:memory:demoDB;create=true";
            this.connection = DriverManager.getConnection(url);
            logConfig("Apache Derby In-Memory Database Initialized");
            ensureConnection();

            inMemoryStorageManager = new InMemoryStorageManager(connection);
            createTablesFromDataObjects();
        } else {
            logConfig("In-Memory Database is disabled");
        }
    }

    private void ensureConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("No current connection available.");
        } else {
            logConfig("Connection with database established");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTablesFromDataObjects() throws SQLException, ClassNotFoundException, IOException {
        // Scan for all classes with @DataObject annotation
        List<Class<?>> dataObjectClasses = ClassFinder.findClasses(WebJavaServer.getAppBasePackage());

        for (Class<?> clazz : dataObjectClasses) {
            if (clazz.isAnnotationPresent(DataObject.class)) {
                inMemoryStorageManager.createTable(clazz);
            }
        }
    }

    public DatabaseManager getDatabaseManager() {
        return inMemoryStorageManager;
    }
}