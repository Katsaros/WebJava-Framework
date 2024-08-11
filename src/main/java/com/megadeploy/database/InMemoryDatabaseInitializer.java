package com.megadeploy.database;

import com.megadeploy.configuration.WebJavaFrameworkConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.megadeploy.utility.LogUtil.*;

public class InMemoryDatabaseInitializer {

    private Connection connection;

    public void initializeDatabase() {
        try {
            WebJavaFrameworkConfiguration config = WebJavaFrameworkConfiguration.loadConfig();
            if (config.isEnableInMemoryDatabase()) {
                logWebJava("In-Memory Database is enabled. Initializing Apache Derby...");

                String url = "jdbc:derby:memory:demoDB;create=true";
                connection = DriverManager.getConnection(url);
                logConfig("Apache Derby In-Memory Database Initialized");
            } else {
                logConfig("In-Memory Database is disabled");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdownDatabase() {
        try {
            if (connection != null) {
                connection.close();
                DriverManager.getConnection("jdbc:derby:memory:demoDB;shutdown=true");
                logWebJava("Apache Derby In-Memory Database Shutdown.");
            }
        } catch (SQLException e) {
            // Handle exceptions during shutdown
            if (e.getErrorCode() == 45000) {
                logWebJava("Database shutdown properly.");
            } else {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
