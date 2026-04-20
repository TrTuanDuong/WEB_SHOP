package com.btl_web.dao;

import com.btl_web.model.DbSupport;
import com.btl_web.model.OrderStore;
import com.btl_web.model.OrderStore.OrderLine;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import com.btl_web.model.User;
import javax.servlet.ServletContext;
public class OrderStoreDAO {
    public static OrderStore.Order createOrder(ServletContext servletContext, User user, List<OrderLine> lines, BigDecimal total) {
        String orderId = UUID.randomUUID().toString();
        LocalDateTime createdAt = LocalDateTime.now();
        
        String insertOrderSql = "INSERT INTO orders (order_id, username, customer_name, shipping_address, status, total, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String insertOrderItemSql = "INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, line_total) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DbSupport.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement orderStatement = connection.prepareStatement(insertOrderSql)) {
                    orderStatement.setString(1, orderId);
                    orderStatement.setString(2, user.getUsername());
                    orderStatement.setString(3, user.getFullName());
                    orderStatement.setString(4, user.getDefaultShippingAddressSummary());
                    orderStatement.setString(5, OrderStore.OrderStatus.CHO_XAC_NHAN.name());
                    orderStatement.setBigDecimal(6, total);
                    orderStatement.setTimestamp(7, Timestamp.valueOf(createdAt));
                    orderStatement.setTimestamp(8, Timestamp.valueOf(createdAt));
                    orderStatement.executeUpdate();
                }
                try (PreparedStatement itemStatement = connection.prepareStatement(insertOrderItemSql)) {
                    for (OrderStore.OrderLine line : lines) {
                        itemStatement.setString(1, orderId);
                        itemStatement.setString(2, line.getProductId());
                        itemStatement.setString(3, line.getProductName());
                        itemStatement.setInt(4, line.getQuantity());
                        itemStatement.setBigDecimal(5, line.getUnitPrice());
                        itemStatement.setBigDecimal(6, line.getLineTotal());
                        itemStatement.addBatch();
                    }
                    itemStatement.executeBatch();
                }
                
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi hệ thống: Không thể tạo đơn hàng.", e);
        }

        return new OrderStore.Order(orderId, user.getUsername(), user.getFullName(), 
                user.getDefaultShippingAddressSummary(), createdAt, total, new ArrayList<>(lines), 
                OrderStore.OrderStatus.CHO_XAC_NHAN);
    }

    public static List<OrderStore.Order> findByUsername(ServletContext servletContext, String username) {
        String sql = "SELECT o.order_id, o.username, o.customer_name, o.shipping_address, o.created_at, o.total, o.status, "
                + "i.product_id, i.product_name, i.quantity, i.unit_price "
                + "FROM orders o LEFT JOIN order_items i ON i.order_id = o.order_id "
                + "WHERE o.username = ? ORDER BY o.created_at DESC, i.product_id ASC";

        Map<String, OrderStore.Order> orderMap = new LinkedHashMap<>();
        try (Connection connection = DbSupport.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String orderId = resultSet.getString("order_id");
                    OrderStore.Order order = orderMap.get(orderId);
                    
                    if (order == null) {
                        Timestamp ts = resultSet.getTimestamp("created_at");
                        LocalDateTime dt = (ts == null) ? LocalDateTime.now() : ts.toLocalDateTime();

                        order = new OrderStore.Order(orderId, resultSet.getString("username"),
                                resultSet.getString("customer_name"), resultSet.getString("shipping_address"),
                                dt, resultSet.getBigDecimal("total"), new ArrayList<>(),
                                parseStatus(resultSet.getString("status")));
                        orderMap.put(orderId, order);
                    }

                    String pId = resultSet.getString("product_id");
                    if (pId != null) {
                        order.getLines().add(new OrderStore.OrderLine(pId, resultSet.getString("product_name"),
                                resultSet.getInt("quantity"), resultSet.getBigDecimal("unit_price")));
                    }
                }
            }
            return Collections.unmodifiableList(new ArrayList<>(orderMap.values()));
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi hệ thống: Không thể truy vấn đơn hàng.", e);
        }
    }

    private static OrderStore.OrderStatus parseStatus(String value) {
        if (value == null) return OrderStore.OrderStatus.CHO_XAC_NHAN;
        try {
            return OrderStore.OrderStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return OrderStore.OrderStatus.CHO_XAC_NHAN;
        }
    }
}