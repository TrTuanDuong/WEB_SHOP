package com.btl_web.model;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class UserStore {
    private UserStore() {
    }

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
                user.getShippingAddresses().addAll(loadAddresses(connection, username));
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
                user.getShippingAddresses().addAll(loadAddresses(connection, username));
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

    private static List<Address> loadAddresses(Connection connection, String username) throws SQLException {
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

    private static boolean isUniqueViolation(SQLException exception) {
        return "23505".equals(exception.getSQLState());
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
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

    public static final class User {
        private final String username;
        private String fullName;
        private final String password;
        private final String roleCode;
        private final String roleName;
        private int age;
        private String gender;
        private String email;
        private String phone;
        private String baseAddress;
        private String defaultAddressId;
        private final List<Address> shippingAddresses;

        public User(String username, String fullName, String password, String roleCode, String roleName) {
            this.username = username;
            this.fullName = fullName;
            this.password = password;
            this.roleCode = roleCode;
            this.roleName = roleName;
            this.shippingAddresses = new ArrayList<>();
            this.age = 0;
            this.gender = "";
            this.email = "";
            this.phone = "";
            this.baseAddress = "";
            this.defaultAddressId = "";
        }

        public String getUsername() {
            return username;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPassword() {
            return password;
        }

        public String getRoleCode() {
            return roleCode;
        }

        public String getRoleName() {
            return roleName;
        }

        public boolean isAdmin() {
            return "ADMIN".equalsIgnoreCase(roleCode);
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getBaseAddress() {
            return baseAddress;
        }

        public void setBaseAddress(String baseAddress) {
            this.baseAddress = baseAddress;
        }

        public List<Address> getShippingAddresses() {
            return shippingAddresses;
        }

        public List<Address> getShippingAddressesView() {
            return Collections.unmodifiableList(shippingAddresses);
        }

        public String getDefaultShippingAddressSummary() {
            for (Address address : shippingAddresses) {
                if (address.getId().equals(defaultAddressId)) {
                    return address.getRecipientName() + " | " + address.getRecipientPhone() + " | "
                            + address.getShippingAddress();
                }
            }
            return "";
        }

        public String getDefaultAddressId() {
            return defaultAddressId;
        }

        public void setDefaultAddressId(String defaultAddressId) {
            this.defaultAddressId = defaultAddressId;
        }
    }

    public static final class Address {
        private final String id;
        private String recipientName;
        private String recipientPhone;
        private String shippingAddress;

        public Address(String id, String recipientName, String recipientPhone, String shippingAddress) {
            this.id = id;
            this.recipientName = recipientName;
            this.recipientPhone = recipientPhone;
            this.shippingAddress = shippingAddress;
        }

        public String getId() {
            return id;
        }

        public String getRecipientName() {
            return recipientName;
        }

        public void setRecipientName(String recipientName) {
            this.recipientName = recipientName;
        }

        public String getRecipientPhone() {
            return recipientPhone;
        }

        public void setRecipientPhone(String recipientPhone) {
            this.recipientPhone = recipientPhone;
        }

        public String getShippingAddress() {
            return shippingAddress;
        }

        public void setShippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
        }
    }
}
