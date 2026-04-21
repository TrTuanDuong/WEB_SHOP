package com.btl_web.dao;

import com.btl_web.model.Address;
import com.btl_web.model.DbSupport;
import com.btl_web.model.User;

import javax.servlet.ServletContext;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UserDAO {
    private static final String USER_DETAIL_SQL = "SELECT u.username, u.full_name, u.password, u.age, u.gender, u.email, u.phone, u.base_address, "
            + "u.default_address_id, u.branch_id, COALESCE(b.branch_name, '') AS branch_name, "
            + "r.role_code, r.role_name, "
            + "COALESCE(sp.total_spending_6m, 0) AS total_spending_6m "
            + "FROM users u "
            + "JOIN roles r ON r.id = u.role_id "
            + "LEFT JOIN branches b ON b.branch_id = u.branch_id "
            + "LEFT JOIN ( "
            + "    SELECT username, COALESCE(SUM(total), 0) AS total_spending_6m "
            + "    FROM orders "
            + "    WHERE created_at >= NOW() - INTERVAL '6 months' "
            + "      AND status NOT IN ('DA_HUY', 'DA_TRA_HANG') "
            + "    GROUP BY username "
            + ") sp ON sp.username = u.username "
            + "WHERE u.username = ?";

    public static boolean register(ServletContext context, String username, String fullName, String password) {
        String sql = "INSERT INTO users (username, full_name, password, role_id, branch_id) "
                + "SELECT ?, ?, ?, id, 'HQ' FROM roles WHERE role_code = 'CUSTOMER'";
        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, fullName);
            statement.setString(3, password);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            if (isUniqueViolation(e)) {
                return false;
            }
            throw new IllegalStateException("Không thể đăng ký tài khoản trong CSDL.", e);
        }
    }

    public static User login(ServletContext context, String username, String password) {
        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(USER_DETAIL_SQL)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                User user = mapUser(resultSet);
                if (!user.getPassword().equals(password)) {
                    return null;
                }
                user.getShippingAddresses().addAll(AddressDAO.loadAddresses(connection, username));
                return user;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đăng nhập từ CSDL.", e);
        }
    }

    public static User findByUsername(ServletContext context, String username) {
        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(USER_DETAIL_SQL)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                User user = mapUser(resultSet);
                user.getShippingAddresses().addAll(AddressDAO.loadAddresses(connection, username));
                return user;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải thông tin người dùng từ CSDL.", e);
        }
    }

    public static OperationResult updateProfile(
            ServletContext context,
            String username,
            String fullName,
            int age,
            String gender,
            String email,
            String phone,
            String baseAddress) {
        String duplicateSql = "SELECT 1 FROM users WHERE username <> ? AND (LOWER(email) = LOWER(?) OR phone = ?) LIMIT 1";
        String updateSql = "UPDATE users "
                + "SET full_name = ?, age = ?, gender = ?, email = ?, phone = ?, base_address = ?, updated_at = NOW() "
                + "WHERE username = ?";

        try (Connection connection = DbSupport.getConnection()) {
            try (PreparedStatement duplicateStatement = connection.prepareStatement(duplicateSql)) {
                duplicateStatement.setString(1, username);
                duplicateStatement.setString(2, email);
                duplicateStatement.setString(3, phone);
                try (ResultSet resultSet = duplicateStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return OperationResult.fail("Email hoặc số điện thoại đã được sử dụng bởi tài khoản khác.");
                    }
                }
            }

            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                updateStatement.setString(1, fullName);
                updateStatement.setInt(2, age);
                updateStatement.setString(3, gender);
                updateStatement.setString(4, email);
                updateStatement.setString(5, phone);
                updateStatement.setString(6, baseAddress);
                updateStatement.setString(7, username);
                int updated = updateStatement.executeUpdate();
                if (updated == 0) {
                    return OperationResult.fail("Không tìm thấy tài khoản.");
                }
                return OperationResult.success("Cập nhật thông tin cá nhân thành công.");
            }
        } catch (SQLException e) {
            if (isUniqueViolation(e)) {
                return OperationResult.fail("Email hoặc số điện thoại đã được sử dụng bởi tài khoản khác.");
            }
            throw new IllegalStateException("Không thể cập nhật thông tin cá nhân trong CSDL.", e);
        }
    }

    public static OperationResult addShippingAddress(
            ServletContext context,
            String username,
            String recipientName,
            String recipientPhone,
            String shippingAddress,
            boolean setDefault) {
        return convertAddressResult(AddressDAO.addShippingAddress(
                context,
                username,
                recipientName,
                recipientPhone,
                shippingAddress,
                setDefault));
    }

    public static OperationResult setDefaultAddress(ServletContext context, String username, String addressId) {
        return convertAddressResult(AddressDAO.setDefaultAddress(context, username, addressId));
    }

    public static OperationResult updateShippingAddress(
            ServletContext context,
            String username,
            String addressId,
            String recipientName,
            String recipientPhone,
            String shippingAddress,
            boolean setDefault) {
        return convertAddressResult(AddressDAO.updateShippingAddress(
                context,
                username,
                addressId,
                recipientName,
                recipientPhone,
                shippingAddress,
                setDefault));
    }

    public static boolean isCheckoutProfileReady(User user) {
        if (user == null) {
            return false;
        }
        if (isBlank(user.getFullName())
                || user.getAge() <= 0
                || isBlank(user.getGender())
                || isBlank(user.getEmail())
                || isBlank(user.getPhone())
                || isBlank(user.getBaseAddress())) {
            return false;
        }

        if (user.getShippingAddresses().isEmpty()) {
            return false;
        }

        if (isBlank(user.getDefaultAddressId())) {
            return false;
        }

        for (Address address : user.getShippingAddresses()) {
            if (address.getId().equals(user.getDefaultAddressId())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFixedProfileLocked(User user) {
        return isCheckoutProfileReady(user);
    }

    public static boolean isAdmin(User user) {
        return isCompanyOwner(user);
    }

    public static boolean isCompanyOwner(User user) {
        return user != null && user.isCompanyOwner();
    }

    public static boolean isBranchOwner(User user) {
        return user != null && user.isBranchOwner();
    }

    public static OperationResult createBranchOwnerAccount(
            ServletContext context,
            String branchId,
            String branchName,
            String branchAddress,
            String ownerUsername,
            String ownerFullName,
            String ownerPassword) {
        String normalizedBranchId = defaultString(branchId).trim().toUpperCase();
        String normalizedBranchName = defaultString(branchName).trim();
        String normalizedBranchAddress = defaultString(branchAddress).trim();
        String normalizedOwnerUsername = defaultString(ownerUsername).trim().toLowerCase();
        String normalizedOwnerFullName = defaultString(ownerFullName).trim();
        String normalizedOwnerPassword = defaultString(ownerPassword).trim();

        if (normalizedBranchId.isEmpty() || normalizedBranchName.isEmpty() || normalizedBranchAddress.isEmpty()
                || normalizedOwnerUsername.isEmpty() || normalizedOwnerFullName.isEmpty()
                || normalizedOwnerPassword.isEmpty()) {
            return OperationResult.fail("Vui lòng nhập đầy đủ thông tin chi nhánh và tài khoản chủ chi nhánh.");
        }

        if (normalizedOwnerUsername.length() < 4) {
            return OperationResult.fail("Tên đăng nhập chủ chi nhánh phải từ 4 ký tự trở lên.");
        }

        if (normalizedOwnerPassword.length() < 6) {
            return OperationResult.fail("Mật khẩu chủ chi nhánh phải từ 6 ký tự trở lên.");
        }

        String insertBranchSql = "INSERT INTO branches (branch_id, branch_code, branch_name, branch_address, owner_username) "
                + "VALUES (?, ?, ?, ?, ?)";
        String insertOwnerSql = "INSERT INTO users (username, full_name, password, role_id, branch_id) "
                + "SELECT ?, ?, ?, id, ? FROM roles WHERE role_code = 'BRANCH_OWNER'";

        try (Connection connection = DbSupport.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement branchStatement = connection.prepareStatement(insertBranchSql)) {
                    branchStatement.setString(1, normalizedBranchId);
                    branchStatement.setString(2, normalizedBranchId);
                    branchStatement.setString(3, normalizedBranchName);
                    branchStatement.setString(4, normalizedBranchAddress);
                    branchStatement.setString(5, normalizedOwnerUsername);
                    branchStatement.executeUpdate();
                }

                try (PreparedStatement ownerStatement = connection.prepareStatement(insertOwnerSql)) {
                    ownerStatement.setString(1, normalizedOwnerUsername);
                    ownerStatement.setString(2, normalizedOwnerFullName);
                    ownerStatement.setString(3, normalizedOwnerPassword);
                    ownerStatement.setString(4, normalizedBranchId);
                    int inserted = ownerStatement.executeUpdate();
                    if (inserted == 0) {
                        throw new SQLException("Không tìm thấy role BRANCH_OWNER để tạo tài khoản chi nhánh.");
                    }
                }

                connection.commit();
                return OperationResult.success("Đã tạo chi nhánh " + normalizedBranchName
                        + " và tài khoản đăng nhập " + normalizedOwnerUsername + ".");
            } catch (SQLException ex) {
                connection.rollback();
                if (isUniqueViolation(ex)) {
                    return OperationResult.fail(
                            "Mã chi nhánh hoặc tài khoản chủ chi nhánh đã tồn tại. Vui lòng dùng giá trị khác.");
                }
                throw ex;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tạo chi nhánh và tài khoản chủ chi nhánh trong CSDL.", e);
        }
    }

    public static List<BranchOwnerAccount> listBranchOwnerAccounts(ServletContext context) {
        String sql = "SELECT b.branch_id, b.branch_name, COALESCE(u.username, b.owner_username) AS owner_username "
                + "FROM branches b "
                + "LEFT JOIN users u ON u.username = b.owner_username "
                + "LEFT JOIN roles r ON r.id = u.role_id "
                + "WHERE u.username IS NULL OR r.role_code = 'BRANCH_OWNER' "
                + "ORDER BY b.branch_name";

        List<BranchOwnerAccount> accounts = new ArrayList<>();
        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                accounts.add(new BranchOwnerAccount(
                        defaultString(resultSet.getString("branch_id")),
                        defaultString(resultSet.getString("branch_name")),
                        defaultString(resultSet.getString("owner_username"))));
            }
            return Collections.unmodifiableList(accounts);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách tài khoản chi nhánh.", e);
        }
    }

    private static User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User(
                resultSet.getString("username"),
                resultSet.getString("full_name"),
                resultSet.getString("password"),
                defaultString(resultSet.getString("role_code")),
                defaultString(resultSet.getString("role_name")));

        Integer age = (Integer) resultSet.getObject("age");
        user.setAge(age == null ? 0 : age);
        user.setGender(defaultString(resultSet.getString("gender")));
        user.setEmail(defaultString(resultSet.getString("email")));
        user.setPhone(defaultString(resultSet.getString("phone")));
        user.setBaseAddress(defaultString(resultSet.getString("base_address")));
        user.setDefaultAddressId(defaultString(resultSet.getString("default_address_id")));
        user.setBranchId(defaultString(resultSet.getString("branch_id")));
        user.setBranchName(defaultString(resultSet.getString("branch_name")));

        BigDecimal spending = resultSet.getBigDecimal("total_spending_6m");
        if (spending == null) {
            spending = BigDecimal.ZERO;
        }
        user.setSpendingLast6Months(spending);

        String tier = membershipTierFor(spending);
        user.setMembershipTier(tier);
        user.setMembershipDiscountRate(discountRateForTier(tier));
        return user;
    }

    private static String membershipTierFor(BigDecimal spending6m) {
        if (spending6m.compareTo(new BigDecimal("15000000")) >= 0) {
            return "DIAMOND";
        }
        if (spending6m.compareTo(new BigDecimal("10000000")) >= 0) {
            return "GOLD";
        }
        if (spending6m.compareTo(new BigDecimal("5000000")) >= 0) {
            return "SILVER";
        }
        return "STANDARD";
    }

    private static BigDecimal discountRateForTier(String tier) {
        if ("DIAMOND".equalsIgnoreCase(tier)) {
            return new BigDecimal("0.12");
        }
        if ("GOLD".equalsIgnoreCase(tier)) {
            return new BigDecimal("0.07");
        }
        if ("SILVER".equalsIgnoreCase(tier)) {
            return new BigDecimal("0.03");
        }
        return BigDecimal.ZERO;
    }

    private static boolean isUniqueViolation(SQLException exception) {
        return "23505".equals(exception.getSQLState());
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static OperationResult convertAddressResult(AddressDAO.OperationResult result) {
        return result.isSuccess() ? OperationResult.success(result.getMessage())
                : OperationResult.fail(result.getMessage());
    }

    public static final class OperationResult {
        private final boolean success;
        private final String message;

        private OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static OperationResult success(String message) {
            return new OperationResult(true, message);
        }

        public static OperationResult fail(String message) {
            return new OperationResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    public static final class BranchOwnerAccount {
        private final String branchId;
        private final String branchName;
        private final String ownerUsername;

        public BranchOwnerAccount(String branchId, String branchName, String ownerUsername) {
            this.branchId = branchId;
            this.branchName = branchName;
            this.ownerUsername = ownerUsername;
        }

        public String getBranchId() {
            return branchId;
        }

        public String getBranchName() {
            return branchName;
        }

        public String getOwnerUsername() {
            return ownerUsername;
        }
    }
}
