package com.btl_web.dao;

import com.btl_web.model.Address;
import com.btl_web.model.DbSupport;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AddressDAO {
    private AddressDAO() {
    }

    public static List<Address> loadAddresses(Connection connection, String username) throws SQLException {
        String sql = "SELECT address_id, recipient_name, recipient_phone, shipping_address "
                + "FROM user_shipping_address WHERE username = ? ORDER BY created_at";
        List<Address> addresses = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    addresses.add(new Address(
                            resultSet.getString("address_id"),
                            resultSet.getString("recipient_name"),
                            resultSet.getString("recipient_phone"),
                            resultSet.getString("shipping_address")));
                }
            }
        }
        return addresses;
    }

    public static OperationResult addShippingAddress(
            ServletContext context,
            String username,
            String recipientName,
            String recipientPhone,
            String shippingAddress,
            boolean setDefault) {
        String addressId = UUID.randomUUID().toString();
        String insertAddressSql = "INSERT INTO user_shipping_address "
                + "(address_id, username, recipient_name, recipient_phone, shipping_address, is_default) "
                + "VALUES (?, ?, ?, ?, ?, FALSE)";
        String hasDefaultSql = "SELECT default_address_id FROM users WHERE username = ?";

        try (Connection connection = DbSupport.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!userExists(connection, username)) {
                    connection.rollback();
                    return OperationResult.fail("Không tìm thấy tài khoản.");
                }

                try (PreparedStatement insertAddressStatement = connection.prepareStatement(insertAddressSql)) {
                    insertAddressStatement.setString(1, addressId);
                    insertAddressStatement.setString(2, username);
                    insertAddressStatement.setString(3, recipientName);
                    insertAddressStatement.setString(4, recipientPhone);
                    insertAddressStatement.setString(5, shippingAddress);
                    insertAddressStatement.executeUpdate();
                }

                boolean shouldSetDefault = setDefault;
                if (!shouldSetDefault) {
                    try (PreparedStatement hasDefaultStatement = connection.prepareStatement(hasDefaultSql)) {
                        hasDefaultStatement.setString(1, username);
                        try (ResultSet resultSet = hasDefaultStatement.executeQuery()) {
                            if (resultSet.next()) {
                                shouldSetDefault = isBlank(resultSet.getString("default_address_id"));
                            }
                        }
                    }
                }

                if (shouldSetDefault) {
                    setDefaultAddressInternal(connection, username, addressId);
                }

                connection.commit();
                return OperationResult.success("Đã thêm địa chỉ giao hàng.");
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể thêm địa chỉ giao hàng trong CSDL.", e);
        }
    }

    public static OperationResult setDefaultAddress(ServletContext context, String username, String addressId) {
        try (Connection connection = DbSupport.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!userExists(connection, username)) {
                    connection.rollback();
                    return OperationResult.fail("Không tìm thấy tài khoản.");
                }
                if (!addressExists(connection, username, addressId)) {
                    connection.rollback();
                    return OperationResult.fail("Địa chỉ giao hàng không hợp lệ.");
                }
                setDefaultAddressInternal(connection, username, addressId);
                connection.commit();
                return OperationResult.success("Đã đặt địa chỉ mặc định.");
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đặt địa chỉ mặc định trong CSDL.", e);
        }
    }

    public static OperationResult updateShippingAddress(
            ServletContext context,
            String username,
            String addressId,
            String recipientName,
            String recipientPhone,
            String shippingAddress,
            boolean setDefault) {
        String updateSql = "UPDATE user_shipping_address "
                + "SET recipient_name = ?, recipient_phone = ?, shipping_address = ?, updated_at = NOW() "
                + "WHERE username = ? AND address_id = ?";

        try (Connection connection = DbSupport.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!userExists(connection, username)) {
                    connection.rollback();
                    return OperationResult.fail("Không tìm thấy tài khoản.");
                }

                int updated;
                try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                    updateStatement.setString(1, recipientName);
                    updateStatement.setString(2, recipientPhone);
                    updateStatement.setString(3, shippingAddress);
                    updateStatement.setString(4, username);
                    updateStatement.setString(5, addressId);
                    updated = updateStatement.executeUpdate();
                }

                if (updated == 0) {
                    connection.rollback();
                    return OperationResult.fail("Địa chỉ giao hàng không hợp lệ.");
                }

                if (setDefault) {
                    setDefaultAddressInternal(connection, username, addressId);
                }

                connection.commit();
                return OperationResult.success("Đã cập nhật địa chỉ giao hàng.");
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật địa chỉ giao hàng trong CSDL.", e);
        }
    }

    private static boolean userExists(Connection connection, String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static boolean addressExists(Connection connection, String username, String addressId) throws SQLException {
        String sql = "SELECT 1 FROM user_shipping_address WHERE username = ? AND address_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, addressId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static void setDefaultAddressInternal(Connection connection, String username, String addressId)
            throws SQLException {
        try (PreparedStatement clearStatement = connection.prepareStatement(
                "UPDATE user_shipping_address SET is_default = FALSE, updated_at = NOW() WHERE username = ?")) {
            clearStatement.setString(1, username);
            clearStatement.executeUpdate();
        }

        try (PreparedStatement markDefaultStatement = connection.prepareStatement(
                "UPDATE user_shipping_address SET is_default = TRUE, updated_at = NOW() WHERE username = ? AND address_id = ?")) {
            markDefaultStatement.setString(1, username);
            markDefaultStatement.setString(2, addressId);
            markDefaultStatement.executeUpdate();
        }

        try (PreparedStatement updateUserStatement = connection.prepareStatement(
                "UPDATE users SET default_address_id = ?, updated_at = NOW() WHERE username = ?")) {
            updateUserStatement.setString(1, addressId);
            updateUserStatement.setString(2, username);
            updateUserStatement.executeUpdate();
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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