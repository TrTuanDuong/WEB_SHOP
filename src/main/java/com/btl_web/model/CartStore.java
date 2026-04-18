package com.btl_web.model;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CartStore {
    private CartStore() {
    }

    public static Map<String, Integer> getCartByUsername(ServletContext context, String username) {
        String sql = "SELECT product_id, quantity FROM cart_items WHERE username = ? ORDER BY created_at";
        Map<String, Integer> cart = new LinkedHashMap<>();
        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    cart.put(resultSet.getString("product_id"), resultSet.getInt("quantity"));
                }
            }
            return cart;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải giỏ hàng từ CSDL.", e);
        }
    }

    public static int totalQuantityByUsername(ServletContext context, String username) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) AS total_quantity FROM cart_items WHERE username = ?";
        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total_quantity");
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tính tổng số lượng giỏ hàng từ CSDL.", e);
        }
    }

    public static void addItem(ServletContext context, String username, String productId, int quantityToAdd) {
        String sql = "INSERT INTO cart_items (username, product_id, quantity) VALUES (?, ?, ?) "
                + "ON CONFLICT (username, product_id) DO UPDATE "
                + "SET quantity = cart_items.quantity + EXCLUDED.quantity, updated_at = NOW()";
        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, productId);
            statement.setInt(3, quantityToAdd);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể thêm sản phẩm vào giỏ hàng trong CSDL.", e);
        }
    }

    public static void removeItem(ServletContext context, String username, String productId) {
        String sql = "DELETE FROM cart_items WHERE username = ? AND product_id = ?";
        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, productId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xoá sản phẩm khỏi giỏ hàng trong CSDL.", e);
        }
    }

    public static void removeItems(ServletContext context, String username, List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }

        String sql = "DELETE FROM cart_items WHERE username = ? AND product_id = ?";
        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            for (String productId : productIds) {
                statement.setString(1, username);
                statement.setString(2, productId);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật giỏ hàng trong CSDL.", e);
        }
    }
}
