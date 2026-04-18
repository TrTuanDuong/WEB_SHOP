package com.btl_web.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbSupport {
    private static final String DB_URL = System.getenv().getOrDefault("DB_URL",
            "jdbc:postgresql://localhost:5432/btl_web");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "postgres");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "postgres");
    private static volatile boolean initialized = false;

    private DbSupport() {
    }

    public static Connection getConnection() throws SQLException {
        initDriverIfNeeded();
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static synchronized void initDriverIfNeeded() {
        if (initialized) {
            return;
        }
        try {
            Class.forName("org.postgresql.Driver");
            initialized = true;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Không tìm thấy PostgreSQL JDBC driver.", e);
        }
    }
}
