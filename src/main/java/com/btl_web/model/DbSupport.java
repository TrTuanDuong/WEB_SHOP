package com.btl_web.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class DbSupport {
    private static final String DB_URL = System.getenv().getOrDefault("DB_URL",
            "jdbc:postgresql://localhost:5432/btl_web");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER",
        System.getenv().getOrDefault("USER", "postgres"));
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");
    private static volatile boolean initialized = false;
    private static volatile boolean schemaSynced = false;

    private DbSupport() {
    }

    public static Connection getConnection() throws SQLException {
        initDriverIfNeeded();
        ensureSchemaSynced();
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

    private static synchronized void ensureSchemaSynced() throws SQLException {
        if (schemaSynced) {
            return;
        }

        String script = loadSchemaScript();
        if (script.isEmpty()) {
            schemaSynced = true;
            return;
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            connection.setAutoCommit(false);
            try {
                for (String statementSql : splitSqlStatements(script)) {
                    try (PreparedStatement statement = connection.prepareStatement(statementSql)) {
                        statement.execute();
                    }
                }
                connection.commit();
                schemaSynced = true;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private static String loadSchemaScript() {
        try (Scanner scanner = new Scanner(
                DbSupport.class.getClassLoader().getResourceAsStream("schema.sql"),
                java.nio.charset.StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tải schema.sql từ resources.", ex);
        }
    }

    private static List<String> splitSqlStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDollarQuote = false;
        for (int i = 0; i < script.length(); i++) {
            char ch = script.charAt(i);

            if (!inSingleQuote && i + 1 < script.length()
                    && script.charAt(i) == '$'
                    && script.charAt(i + 1) == '$') {
                inDollarQuote = !inDollarQuote;
                current.append("$$");
                i++;
                continue;
            }

            if (!inDollarQuote && ch == '\'') {
                inSingleQuote = !inSingleQuote;
            }

            if (ch == ';' && !inSingleQuote && !inDollarQuote) {
                String sql = current.toString().trim();
                if (!sql.isEmpty()) {
                    statements.add(sql);
                }
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }
        String last = current.toString().trim();
        if (!last.isEmpty()) {
            statements.add(last);
        }
        return statements;
    }
}
