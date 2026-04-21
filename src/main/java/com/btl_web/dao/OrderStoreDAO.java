package com.btl_web.dao;

import com.btl_web.model.DbSupport;
import com.btl_web.model.OrderStore;
import com.btl_web.model.OrderStore.OrderLine;
import com.btl_web.model.User;

import javax.servlet.ServletContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderStoreDAO {
    public static OrderStore.Order createOrder(
            ServletContext servletContext,
            User user,
            List<OrderLine> lines,
            BigDecimal subtotal,
            BigDecimal discountRate,
            String memberTier) {
        String orderId = UUID.randomUUID().toString();
        LocalDateTime createdAt = LocalDateTime.now();

        BigDecimal normalizedRate = discountRate == null ? BigDecimal.ZERO : discountRate;
        BigDecimal discountAmount = subtotal.multiply(normalizedRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(discountAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        String insertOrderSql = "INSERT INTO orders ("
                + "order_id, username, branch_id, customer_name, shipping_address, status, "
                + "subtotal, discount_rate, discount_amount, member_tier_snapshot, total, created_at, updated_at"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertOrderItemSql = "INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, line_total) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DbSupport.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement orderStatement = connection.prepareStatement(insertOrderSql)) {
                    orderStatement.setString(1, orderId);
                    orderStatement.setString(2, user.getUsername());
                    orderStatement.setString(3, defaultBranchId(user.getBranchId()));
                    orderStatement.setString(4, user.getFullName());
                    orderStatement.setString(5, user.getDefaultShippingAddressSummary());
                    orderStatement.setString(6, OrderStore.OrderStatus.CHO_XAC_NHAN.name());
                    orderStatement.setBigDecimal(7, subtotal);
                    orderStatement.setBigDecimal(8, normalizedRate);
                    orderStatement.setBigDecimal(9, discountAmount);
                    orderStatement.setString(10, memberTier == null ? "STANDARD" : memberTier);
                    orderStatement.setBigDecimal(11, total);
                    orderStatement.setTimestamp(12, Timestamp.valueOf(createdAt));
                    orderStatement.setTimestamp(13, Timestamp.valueOf(createdAt));
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

        return new OrderStore.Order(
                orderId,
                user.getUsername(),
                defaultBranchId(user.getBranchId()),
                user.getFullName(),
                user.getDefaultShippingAddressSummary(),
                createdAt,
                subtotal,
                normalizedRate,
                discountAmount,
                memberTier == null ? "STANDARD" : memberTier,
                total,
                new ArrayList<>(lines),
                OrderStore.OrderStatus.CHO_XAC_NHAN);
    }

    public static List<OrderStore.Order> findByUsername(ServletContext servletContext, String username) {
        String sql = "SELECT o.order_id, o.username, o.branch_id, o.customer_name, o.shipping_address, o.created_at, "
                + "o.subtotal, o.discount_rate, o.discount_amount, o.member_tier_snapshot, o.total, o.status, "
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

                        order = new OrderStore.Order(
                                orderId,
                                resultSet.getString("username"),
                                resultSet.getString("branch_id"),
                                resultSet.getString("customer_name"),
                                resultSet.getString("shipping_address"),
                                dt,
                                safeMoney(resultSet.getBigDecimal("subtotal")),
                                safeMoney(resultSet.getBigDecimal("discount_rate")),
                                safeMoney(resultSet.getBigDecimal("discount_amount")),
                                defaultText(resultSet.getString("member_tier_snapshot"), "STANDARD"),
                                safeMoney(resultSet.getBigDecimal("total")),
                                new ArrayList<>(),
                                parseStatus(resultSet.getString("status")));
                        orderMap.put(orderId, order);
                    }

                    String pId = resultSet.getString("product_id");
                    if (pId != null) {
                        order.getLines().add(new OrderStore.OrderLine(
                                pId,
                                resultSet.getString("product_name"),
                                resultSet.getInt("quantity"),
                                resultSet.getBigDecimal("unit_price")));
                    }
                }
            }
            return Collections.unmodifiableList(new ArrayList<>(orderMap.values()));
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi hệ thống: Không thể truy vấn đơn hàng.", e);
        }
    }

    public static List<OrderStore.Order> findByBranchId(ServletContext servletContext, String branchId, int limit) {
        String sql = "SELECT o.order_id, o.username, o.branch_id, o.customer_name, o.shipping_address, o.created_at, "
                + "o.subtotal, o.discount_rate, o.discount_amount, o.member_tier_snapshot, o.total, o.status, "
                + "i.product_id, i.product_name, i.quantity, i.unit_price "
                + "FROM orders o LEFT JOIN order_items i ON i.order_id = o.order_id "
                + "WHERE o.branch_id = ? ORDER BY o.created_at DESC, i.product_id ASC";

        Map<String, OrderStore.Order> orderMap = new LinkedHashMap<>();
        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, branchId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String orderId = resultSet.getString("order_id");
                    OrderStore.Order order = orderMap.get(orderId);

                    if (order == null) {
                        if (orderMap.size() >= Math.max(limit, 1)) {
                            break;
                        }
                        Timestamp ts = resultSet.getTimestamp("created_at");
                        LocalDateTime dt = (ts == null) ? LocalDateTime.now() : ts.toLocalDateTime();
                        order = new OrderStore.Order(
                                orderId,
                                resultSet.getString("username"),
                                resultSet.getString("branch_id"),
                                resultSet.getString("customer_name"),
                                resultSet.getString("shipping_address"),
                                dt,
                                safeMoney(resultSet.getBigDecimal("subtotal")),
                                safeMoney(resultSet.getBigDecimal("discount_rate")),
                                safeMoney(resultSet.getBigDecimal("discount_amount")),
                                defaultText(resultSet.getString("member_tier_snapshot"), "STANDARD"),
                                safeMoney(resultSet.getBigDecimal("total")),
                                new ArrayList<>(),
                                parseStatus(resultSet.getString("status")));
                        orderMap.put(orderId, order);
                    }

                    String pId = resultSet.getString("product_id");
                    if (pId != null) {
                        order.getLines().add(new OrderStore.OrderLine(
                                pId,
                                resultSet.getString("product_name"),
                                resultSet.getInt("quantity"),
                                resultSet.getBigDecimal("unit_price")));
                    }
                }
            }
            return Collections.unmodifiableList(new ArrayList<>(orderMap.values()));
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi hệ thống: Không thể truy vấn đơn hàng theo chi nhánh.", e);
        }
    }

    private static String defaultBranchId(String branchId) {
        if (branchId == null || branchId.trim().isEmpty()) {
            return "HQ";
        }
        return branchId;
    }

    private static BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String defaultText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }

    private static OrderStore.OrderStatus parseStatus(String value) {
        if (value == null) {
            return OrderStore.OrderStatus.CHO_XAC_NHAN;
        }
        try {
            return OrderStore.OrderStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return OrderStore.OrderStatus.CHO_XAC_NHAN;
        }
    }
}
