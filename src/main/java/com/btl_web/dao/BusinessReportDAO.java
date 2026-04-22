package com.btl_web.dao;

import com.btl_web.model.DbSupport;

import javax.servlet.ServletContext;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BusinessReportDAO {
    private BusinessReportDAO() {
    }

    public static CompanySummary companySummary(ServletContext context) {
        String sql = "SELECT "
                + "COALESCE((SELECT SUM(total) FROM orders WHERE status NOT IN ('DA_HUY', 'DA_TRA_HANG')), 0) AS total_revenue, "
                + "COALESCE((SELECT SUM(stock_quantity) FROM clothing_product), 0) AS total_inventory, "
                + "COALESCE((SELECT COUNT(*) FROM clothing_product), 0) AS total_products";

        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                return new CompanySummary(BigDecimal.ZERO, 0, 0);
            }
            return new CompanySummary(
                    safeMoney(resultSet.getBigDecimal("total_revenue")),
                    resultSet.getInt("total_inventory"),
                    resultSet.getInt("total_products"));
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải tổng quan công ty.", e);
        }
    }

    public static List<CategoryStat> categoryStats(ServletContext context) {
        String sql = "SELECT category, COUNT(*) AS sku_count, COALESCE(SUM(stock_quantity), 0) AS total_stock "
                + "FROM clothing_product "
                + "GROUP BY category "
                + "ORDER BY category";

        List<CategoryStat> rows = new ArrayList<>();
        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rows.add(new CategoryStat(
                        resultSet.getString("category"),
                        resultSet.getInt("sku_count"),
                        resultSet.getInt("total_stock")));
            }
            return Collections.unmodifiableList(rows);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải thống kê theo phân loại.", e);
        }
    }

    public static List<BranchStat> branchStats(ServletContext context) {
        String sql = "SELECT b.branch_id, b.branch_name, b.branch_address, b.owner_username, "
                + "COALESCE(o.order_count, 0) AS order_count, "
                + "COALESCE(o.revenue, 0) AS revenue, "
                + "COALESCE(i.total_inventory, 0) AS total_inventory, "
                + "COALESCE(i.product_count, 0) AS product_count "
                + "FROM branches b "
                + "LEFT JOIN ( "
                + "  SELECT branch_id, COUNT(*) AS order_count, COALESCE(SUM(total), 0) AS revenue "
                + "  FROM orders "
                + "  WHERE status NOT IN ('DA_HUY', 'DA_TRA_HANG') "
                + "  GROUP BY branch_id "
                + ") o ON o.branch_id = b.branch_id "
                + "LEFT JOIN ( "
                + "  SELECT branch_id, COALESCE(SUM(stock_quantity), 0) AS total_inventory, COUNT(*) AS product_count "
                + "  FROM shop_product "
                + "  GROUP BY branch_id "
                + ") i ON i.branch_id = b.branch_id "
                + "ORDER BY b.branch_name";

        List<BranchStat> rows = new ArrayList<>();
        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rows.add(new BranchStat(
                        resultSet.getString("branch_id"),
                        resultSet.getString("branch_name"),
                        resultSet.getString("branch_address"),
                        resultSet.getString("owner_username"),
                        resultSet.getInt("order_count"),
                        safeMoney(resultSet.getBigDecimal("revenue")),
                        resultSet.getInt("total_inventory"),
                        resultSet.getInt("product_count")));
            }
            return Collections.unmodifiableList(rows);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải thống kê chi nhánh.", e);
        }
    }

    public static BranchStat branchStatByOwner(ServletContext context, String ownerUsername) {
        String sql = "SELECT b.branch_id, b.branch_name, b.branch_address, b.owner_username, "
                + "COALESCE(o.order_count, 0) AS order_count, "
                + "COALESCE(o.revenue, 0) AS revenue, "
                + "COALESCE(i.total_inventory, 0) AS total_inventory, "
                + "COALESCE(i.product_count, 0) AS product_count "
                + "FROM branches b "
                + "LEFT JOIN ( "
                + "  SELECT branch_id, COUNT(*) AS order_count, COALESCE(SUM(total), 0) AS revenue "
                + "  FROM orders "
                + "  WHERE status NOT IN ('DA_HUY', 'DA_TRA_HANG') "
                + "  GROUP BY branch_id "
                + ") o ON o.branch_id = b.branch_id "
                + "LEFT JOIN ( "
                + "  SELECT branch_id, COALESCE(SUM(stock_quantity), 0) AS total_inventory, COUNT(*) AS product_count "
                + "  FROM shop_product "
                + "  GROUP BY branch_id "
                + ") i ON i.branch_id = b.branch_id "
                + "WHERE b.owner_username = ? LIMIT 1";

        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ownerUsername);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new BranchStat(
                        resultSet.getString("branch_id"),
                        resultSet.getString("branch_name"),
                        resultSet.getString("branch_address"),
                        resultSet.getString("owner_username"),
                        resultSet.getInt("order_count"),
                        safeMoney(resultSet.getBigDecimal("revenue")),
                        resultSet.getInt("total_inventory"),
                        resultSet.getInt("product_count"));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải thống kê chi nhánh của bạn.", e);
        }
    }

    private static BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public static final class CompanySummary {
        private final BigDecimal totalRevenue;
        private final int totalInventory;
        private final int totalProducts;

        public CompanySummary(BigDecimal totalRevenue, int totalInventory, int totalProducts) {
            this.totalRevenue = totalRevenue;
            this.totalInventory = totalInventory;
            this.totalProducts = totalProducts;
        }

        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }

        public int getTotalInventory() {
            return totalInventory;
        }

        public int getTotalProducts() {
            return totalProducts;
        }
    }

    public static final class CategoryStat {
        private final String category;
        private final int skuCount;
        private final int totalStock;

        public CategoryStat(String category, int skuCount, int totalStock) {
            this.category = category;
            this.skuCount = skuCount;
            this.totalStock = totalStock;
        }

        public String getCategory() {
            return category;
        }

        public int getSkuCount() {
            return skuCount;
        }

        public int getTotalStock() {
            return totalStock;
        }
    }

    public static final class BranchStat {
        private final String branchId;
        private final String branchName;
        private final String branchAddress;
        private final String ownerUsername;
        private final int orderCount;
        private final BigDecimal revenue;
        private final int totalInventory;
        private final int productCount;

        public BranchStat(
                String branchId,
                String branchName,
                String branchAddress,
                String ownerUsername,
                int orderCount,
                BigDecimal revenue,
                int totalInventory,
                int productCount) {
            this.branchId = branchId;
            this.branchName = branchName;
            this.branchAddress = branchAddress;
            this.ownerUsername = ownerUsername;
            this.orderCount = orderCount;
            this.revenue = revenue;
            this.totalInventory = totalInventory;
            this.productCount = productCount;
        }

        public String getBranchId() {
            return branchId;
        }

        public String getBranchName() {
            return branchName;
        }

        public String getBranchAddress() {
            return branchAddress;
        }

        public String getOwnerUsername() {
            return ownerUsername;
        }

        public int getOrderCount() {
            return orderCount;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public int getTotalInventory() {
            return totalInventory;
        }

        public int getProductCount() {
            return productCount;
        }
    }
}
