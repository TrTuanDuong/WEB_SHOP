package com.btl_web.dao;

import com.btl_web.model.Address;
import com.btl_web.model.DbSupport;
import com.btl_web.model.User;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public final class UserDAO {
    public static boolean register(ServletContext context, String username, String fullName, String password) {
        String sql = "INSERT INTO users (username, full_name, password, role_id) "
                + "SELECT ?, ?, ?, id FROM roles WHERE role_code = 'CUSTOMER'";
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
        String sql = "SELECT u.username, u.full_name, u.password, u.age, u.gender, u.email, u.phone, u.base_address, "
                + "u.default_address_id, r.role_code, r.role_name "
                + "FROM users u JOIN roles r ON r.id = u.role_id "
                + "WHERE u.username = ?";
        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
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
        String sql = "SELECT u.username, u.full_name, u.password, u.age, u.gender, u.email, u.phone, u.base_address, "
                + "u.default_address_id, r.role_code, r.role_name "
                + "FROM users u JOIN roles r ON r.id = u.role_id "
                + "WHERE u.username = ?";
        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
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
        return user != null && user.isAdmin();
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
        return user;
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
        return result.isSuccess() ? OperationResult.success(result.getMessage()) : OperationResult.fail(result.getMessage());
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
}