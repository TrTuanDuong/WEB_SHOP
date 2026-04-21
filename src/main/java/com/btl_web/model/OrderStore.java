package com.btl_web.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class OrderStore {

    private OrderStore() {
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
        private final String branchId;
        private final String customerName;
        private final String shippingAddress;
        private final LocalDateTime createdAt;
        private final BigDecimal subtotal;
        private final BigDecimal discountRate;
        private final BigDecimal discountAmount;
        private final String memberTierSnapshot;
        private final BigDecimal total;
        private final List<OrderLine> lines; // DAO sẽ thêm dữ liệu vào đây
        private final OrderStatus status;

        public Order(
                String id,
                String username,
                String branchId,
                String customerName,
                String shippingAddress,
                LocalDateTime createdAt,
                BigDecimal subtotal,
                BigDecimal discountRate,
                BigDecimal discountAmount,
                String memberTierSnapshot,
                BigDecimal total,
                List<OrderLine> lines,
                OrderStatus status) {
            this.id = id;
            this.username = username;
            this.branchId = branchId;
            this.customerName = customerName;
            this.shippingAddress = shippingAddress;
            this.createdAt = createdAt;
            this.subtotal = subtotal;
            this.discountRate = discountRate;
            this.discountAmount = discountAmount;
            this.memberTierSnapshot = memberTierSnapshot;
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

        public String getBranchId() {
            return branchId;
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

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public BigDecimal getDiscountRate() {
            return discountRate;
        }

        public BigDecimal getDiscountAmount() {
            return discountAmount;
        }

        public String getMemberTierSnapshot() {
            return memberTierSnapshot;
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
