package com.btl_web.model;

import javax.servlet.ServletContext;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public final class ShopCatalog {
    private static final String DB_URL = System.getenv().getOrDefault("DB_URL",
            "jdbc:postgresql://localhost:5432/btl_web");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "postgres");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "postgres");
    private static volatile boolean schemaInitialized = false;

    private ShopCatalog() {
    }

    public static List<Product> all(ServletContext context) {
        initSchemaIfNeeded();
        String sql = "SELECT id, name, group_name, segment, size, color, price "
                + "FROM shop_product ORDER BY id";
        List<Product> products = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                products.add(new Product(
                        resultSet.getString("id"),
                        resultSet.getString("name"),
                        resultSet.getString("group_name"),
                        resultSet.getString("segment"),
                        resultSet.getString("size"),
                        resultSet.getString("color"),
                        resultSet.getBigDecimal("price")));
            }
            return Collections.unmodifiableList(products);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh mục sản phẩm từ CSDL.", e);
        }
    }

    public static Product findById(ServletContext context, String id) {
        initSchemaIfNeeded();
        String sql = "SELECT id, name, group_name, segment, size, color, price "
                + "FROM shop_product WHERE id = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new Product(
                        resultSet.getString("id"),
                        resultSet.getString("name"),
                        resultSet.getString("group_name"),
                        resultSet.getString("segment"),
                        resultSet.getString("size"),
                        resultSet.getString("color"),
                        resultSet.getBigDecimal("price"));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm sản phẩm theo mã từ CSDL.", e);
        }
    }

    private static synchronized void initSchemaIfNeeded() {
        if (schemaInitialized) {
            return;
        }

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Không tìm thấy PostgreSQL JDBC driver.", e);
        }

        String createTableSql = "CREATE TABLE IF NOT EXISTS shop_product ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT NOT NULL,"
                + "group_name TEXT NOT NULL,"
                + "segment TEXT NOT NULL,"
                + "size TEXT NOT NULL,"
                + "color TEXT NOT NULL,"
                + "price NUMERIC(14, 2) NOT NULL)";

        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(createTableSql)) {
            statement.executeUpdate();
            schemaInitialized = true;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể khởi tạo bảng shop_product.", e);
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static final class Product {
        private final String id;
        private final String name;
        private final String group;
        private final String segment;
        private final String size;
        private final String color;
        private final BigDecimal price;

        public Product(String id, String name, String group, String segment, String size, String color,
                BigDecimal price) {
            this.id = id;
            this.name = name;
            this.group = group;
            this.segment = segment;
            this.size = size;
            this.color = color;
            this.price = price;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getGroup() {
            return group;
        }

        public String getSegment() {
            return segment;
        }

        public String getSize() {
            return size;
        }

        public String getColor() {
            return color;
        }

        public BigDecimal getPrice() {
            return price;
        }
    }
}
