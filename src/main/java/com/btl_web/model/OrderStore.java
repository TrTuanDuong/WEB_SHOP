package com.btl_web.model;

import javax.servlet.ServletContext;
import java.math.BigDecimal;
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

public final class OrderStore {
    private OrderStore() {
    }

    public static Order createOrder(ServletContext context, User user, List<OrderLine> lines,
            BigDecimal total) {
        String orderId = UUID.randomUUID().toString();
        LocalDateTime createdAt = LocalDateTime.now();
        String insertOrderSql = "INSERT INTO orders "
                + "(order_id, username, customer_name, shipping_address, status, total, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String insertOrderItemSql = "INSERT INTO order_items "
                + "(order_id, product_id, product_name, quantity, unit_price, line_total) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DbSupport.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement orderStatement = connection.prepareStatement(insertOrderSql)) {
                    orderStatement.setString(1, orderId);
                    orderStatement.setString(2, user.getUsername());
                    orderStatement.setString(3, user.getFullName());
                    orderStatement.setString(4, user.getDefaultShippingAddressSummary());
                    orderStatement.setString(5, OrderStatus.CHO_XAC_NHAN.name());
                    orderStatement.setBigDecimal(6, total);
                    orderStatement.setTimestamp(7, Timestamp.valueOf(createdAt));
                    orderStatement.setTimestamp(8, Timestamp.valueOf(createdAt));
                    orderStatement.executeUpdate();
                }

                try (PreparedStatement itemStatement = connection.prepareStatement(insertOrderItemSql)) {
                    for (OrderLine line : lines) {
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
            throw new IllegalStateException("Không thể tạo đơn hàng trong CSDL.", e);
        }

        Order order = new Order(
                orderId,
                user.getUsername(),
                user.getFullName(),
                user.getDefaultShippingAddressSummary(),
                createdAt,
                total,
                new ArrayList<>(lines),
                OrderStatus.CHO_XAC_NHAN);
        return order;
    }

    public static List<Order> findByUsername(ServletContext context, String username) {
        String sql = "SELECT o.order_id, o.username, o.customer_name, o.shipping_address, o.created_at, o.total, o.status, "
                + "i.product_id, i.product_name, i.quantity, i.unit_price "
                + "FROM orders o "
                + "LEFT JOIN order_items i ON i.order_id = o.order_id "
                + "WHERE o.username = ? "
                + "ORDER BY o.created_at DESC, i.product_id ASC";

        Map<String, Order> orderMap = new LinkedHashMap<>();
        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String orderId = resultSet.getString("order_id");
                    Order order = orderMap.get(orderId);
                    if (order == null) {
                        Timestamp createdAtValue = resultSet.getTimestamp("created_at");
                        LocalDateTime createdAt = createdAtValue == null
                                ? LocalDateTime.now()
                                : createdAtValue.toLocalDateTime();

                        order = new Order(
                                orderId,
                                resultSet.getString("username"),
                                resultSet.getString("customer_name"),
                                resultSet.getString("shipping_address"),
                                createdAt,
                                resultSet.getBigDecimal("total"),
                                new ArrayList<OrderLine>(),
                                parseStatus(resultSet.getString("status")));
                        orderMap.put(orderId, order);
                    }

                    String productId = resultSet.getString("product_id");
                    if (productId != null) {
                        order.getLines().add(new OrderLine(
                                productId,
                                resultSet.getString("product_name"),
                                resultSet.getInt("quantity"),
                                resultSet.getBigDecimal("unit_price")));
                    }
                }
            }
            return Collections.unmodifiableList(new ArrayList<>(orderMap.values()));
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải đơn hàng từ CSDL.", e);
        }
    }

    private static OrderStatus parseStatus(String value) {
        if (value == null) {
            return OrderStatus.CHO_XAC_NHAN;
        }
        try {
            return OrderStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return OrderStatus.CHO_XAC_NHAN;
        }
    }

    public enum OrderStatus {
        CHO_XAC_NHAN("Chờ xác nhận"),
        DA_XAC_NHAN("Đã xác nhận"),
        DANG_GIAO("Đang giao"),
        DA_GIAO("Đã giao"),
        DA_HUY("Đã huỷ"),
        DA_TRA_HANG("Đã trả hàng");

        private final String label;

        OrderStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static final class OrderLine {
        private final String productId;
        private final String productName;
        private final int quantity;
        private final BigDecimal unitPrice;

        public OrderLine(String productId, String productName, int quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public BigDecimal getLineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public static final class Order {
        private final String id;
        private final String username;
        private final String customerName;
        private final String shippingAddress;
        private final LocalDateTime createdAt;
        private final BigDecimal total;
        private final List<OrderLine> lines;
        private final OrderStatus status;

        public Order(String id, String username, String customerName, String shippingAddress, LocalDateTime createdAt,
                BigDecimal total, List<OrderLine> lines, OrderStatus status) {
            this.id = id;
            this.username = username;
            this.customerName = customerName;
            this.shippingAddress = shippingAddress;
            this.createdAt = createdAt;
            this.total = total;
            this.lines = lines;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getShippingAddress() {
            return shippingAddress;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public List<OrderLine> getLines() {
            return lines;
        }

        public OrderStatus getStatus() {
            return status;
        }
    }
}
