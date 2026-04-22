package com.btl_web.model;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClothingStore {
    private static volatile boolean schemaInitialized = false;

    private ClothingStore() {
    }

    public static boolean exists(String productCode) {
        initSchemaIfNeeded();
        String sql = "SELECT 1 FROM clothing_product WHERE LOWER(product_code) = LOWER(?) LIMIT 1";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, productCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể kiểm tra mã sản phẩm trong CSDL.", e);
        }
    }

    public static void insert(
            String productCode,
            String name,
            String category,
            String size,
            String color,
            BigDecimal price,
            int stockQuantity) {
        initSchemaIfNeeded();
        String sql = "INSERT INTO clothing_product (product_code, name, category, size, color, price, stock_quantity) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, productCode);
            statement.setString(2, name);
            statement.setString(3, category);
            statement.setString(4, size);
            statement.setString(5, color);
            statement.setBigDecimal(6, price);
            statement.setInt(7, stockQuantity);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể thêm sản phẩm vào CSDL.", e);
        }
    }

    public static List<ClothingItem> all() {
        initSchemaIfNeeded();
        String sql = "SELECT product_code, name, category, size, color, price, stock_quantity "
                + "FROM clothing_product ORDER BY product_code";
        List<ClothingItem> items = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                items.add(new ClothingItem(
                        resultSet.getString("product_code"),
                        resultSet.getString("name"),
                        resultSet.getString("category"),
                        resultSet.getString("size"),
                        resultSet.getString("color"),
                        resultSet.getBigDecimal("price"),
                        resultSet.getInt("stock_quantity")));
            }
            return Collections.unmodifiableList(items);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách sản phẩm từ CSDL.", e);
        }
    }

    public static void update(String productCode, String name, String category, String size, String color, BigDecimal price, int stockQuantity) {
        initSchemaIfNeeded();
        String sql = "UPDATE clothing_product SET name = ?, category = ?, size = ?, color = ?, price = ?, stock_quantity = ? WHERE LOWER(product_code) = LOWER(?)";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, category);
            statement.setString(3, size);
            statement.setString(4, color);
            statement.setBigDecimal(5, price);
            statement.setInt(6, stockQuantity);
            statement.setString(7, productCode);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật sản phẩm trong CSDL.", e);
        }
    }

    public static void delete(String productCode) {
        initSchemaIfNeeded();
        String sql = "DELETE FROM clothing_product WHERE LOWER(product_code) = LOWER(?)";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, productCode);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xoá sản phẩm khỏi CSDL.", e);
        }
    }

    public static ClothingItem findByCode(String productCode) {
        initSchemaIfNeeded();
        String sql = "SELECT product_code, name, category, size, color, price, stock_quantity FROM clothing_product WHERE LOWER(product_code) = LOWER(?)";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, productCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new ClothingItem(resultSet.getString("product_code"), resultSet.getString("name"), resultSet.getString("category"), resultSet.getString("size"), resultSet.getString("color"), resultSet.getBigDecimal("price"), resultSet.getInt("stock_quantity"));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm sản phẩm trong CSDL.", e);
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

        String createTableSql = "CREATE TABLE IF NOT EXISTS clothing_product ("
                + "product_code TEXT PRIMARY KEY,"
                + "name TEXT NOT NULL,"
                + "category TEXT NOT NULL,"
                + "size TEXT NOT NULL,"
                + "color TEXT NOT NULL,"
                + "price NUMERIC(14, 2) NOT NULL,"
                + "stock_quantity INTEGER NOT NULL DEFAULT 0)";

        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(createTableSql)) {
            statement.executeUpdate();

            String syncSql = "INSERT INTO clothing_product (product_code, name, category, size, color, price, stock_quantity) "
                    + "SELECT id, name, group_name, size, color, price, stock_quantity FROM shop_product "
                    + "ON CONFLICT (product_code) DO UPDATE SET "
                    + "name = EXCLUDED.name, "
                    + "category = EXCLUDED.category, "
                    + "size = EXCLUDED.size, "
                    + "color = EXCLUDED.color, "
                    + "price = EXCLUDED.price, "
                    + "stock_quantity = EXCLUDED.stock_quantity";
            try (PreparedStatement syncStatement = connection.prepareStatement(syncSql)) {
                syncStatement.executeUpdate();
            }

            String pruneSql = "DELETE FROM clothing_product cp "
                    + "WHERE NOT EXISTS (SELECT 1 FROM shop_product sp WHERE sp.id = cp.product_code)";
            try (PreparedStatement pruneStatement = connection.prepareStatement(pruneSql)) {
                pruneStatement.executeUpdate();
            }
            schemaInitialized = true;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể khởi tạo bảng clothing_product.", e);
        }
    }

    private static Connection getConnection() throws SQLException {
        return DbSupport.getConnection();
    }

    public static final class ClothingItem {
        private final String productCode;
        private final String name;
        private final String category;
        private final String size;
        private final String color;
        private final BigDecimal price;
        private final int stockQuantity;

        public ClothingItem(
                String productCode,
                String name,
                String category,
                String size,
                String color,
                BigDecimal price,
                int stockQuantity) {
            this.productCode = productCode;
            this.name = name;
            this.category = category;
            this.size = size;
            this.color = color;
            this.price = price;
            this.stockQuantity = stockQuantity;
        }

        public String getProductCode() {
            return productCode;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
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

        public int getStockQuantity() {
            return stockQuantity;
        }
    }
}
