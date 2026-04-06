package com.btl_web;

import javax.servlet.ServletContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class OrderStore {
    private static final String ORDERS_KEY = "shopOrders";

    private OrderStore() {
    }

    public static Order createOrder(ServletContext context, UserStore.User user, List<OrderLine> lines,
            BigDecimal total) {
        Order order = new Order(
                UUID.randomUUID().toString(),
                user.getUsername(),
                user.getFullName(),
                user.getDefaultShippingAddressSummary(),
                LocalDateTime.now(),
                total,
                new ArrayList<>(lines),
                OrderStatus.CHO_XAC_NHAN);
        getOrders(context).add(order);
        return order;
    }

    public static List<Order> findByUsername(ServletContext context, String username) {
        List<Order> matches = new ArrayList<>();
        for (Order order : getOrders(context)) {
            if (order.getUsername().equals(username)) {
                matches.add(order);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    private static List<Order> getOrders(ServletContext context) {
        synchronized (context) {
            @SuppressWarnings("unchecked")
            List<Order> orders = (List<Order>) context.getAttribute(ORDERS_KEY);
            if (orders == null) {
                orders = new ArrayList<>();
                context.setAttribute(ORDERS_KEY, orders);
            }
            return orders;
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
