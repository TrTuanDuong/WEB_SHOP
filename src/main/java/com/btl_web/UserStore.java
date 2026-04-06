package com.btl_web;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class UserStore {
    private static final String USERS_KEY = "shopUsers";

    private UserStore() {
    }

    public static boolean register(ServletContext context, String username, String fullName, String password) {
        Map<String, User> users = getUsers(context);
        synchronized (users) {
            if (users.containsKey(username)) {
                return false;
            }
            users.put(username, new User(username, fullName, password));
            return true;
        }
    }

    public static User login(ServletContext context, String username, String password) {
        Map<String, User> users = getUsers(context);
        User user = users.get(username);
        if (user == null) {
            return null;
        }
        return user.getPassword().equals(password) ? user : null;
    }

    public static User findByUsername(ServletContext context, String username) {
        return getUsers(context).get(username);
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
        Map<String, User> users = getUsers(context);
        synchronized (users) {
            User current = users.get(username);
            if (current == null) {
                return OperationResult.fail("Không tìm thấy tài khoản.");
            }

            for (User user : users.values()) {
                if (user.getUsername().equals(username)) {
                    continue;
                }
                if (email.equalsIgnoreCase(user.getEmail())) {
                    return OperationResult.fail("Email đã được sử dụng bởi tài khoản khác.");
                }
                if (phone.equals(user.getPhone())) {
                    return OperationResult.fail("Số điện thoại đã được sử dụng bởi tài khoản khác.");
                }
            }

            current.setFullName(fullName);
            current.setAge(age);
            current.setGender(gender);
            current.setEmail(email);
            current.setPhone(phone);
            current.setBaseAddress(baseAddress);
            return OperationResult.success("Cập nhật thông tin cá nhân thành công.");
        }
    }

    public static OperationResult addShippingAddress(
            ServletContext context,
            String username,
            String recipientName,
            String recipientPhone,
            String shippingAddress,
            boolean setDefault) {
        Map<String, User> users = getUsers(context);
        synchronized (users) {
            User current = users.get(username);
            if (current == null) {
                return OperationResult.fail("Không tìm thấy tài khoản.");
            }

            Address address = new Address(
                    UUID.randomUUID().toString(),
                    recipientName,
                    recipientPhone,
                    shippingAddress);

            current.getShippingAddresses().add(address);

            if (setDefault || current.getDefaultAddressId() == null || current.getDefaultAddressId().isEmpty()) {
                current.setDefaultAddressId(address.getId());
            }

            return OperationResult.success("Đã thêm địa chỉ giao hàng.");
        }
    }

    public static OperationResult setDefaultAddress(ServletContext context, String username, String addressId) {
        Map<String, User> users = getUsers(context);
        synchronized (users) {
            User current = users.get(username);
            if (current == null) {
                return OperationResult.fail("Không tìm thấy tài khoản.");
            }

            boolean found = false;
            for (Address address : current.getShippingAddresses()) {
                if (address.getId().equals(addressId)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return OperationResult.fail("Địa chỉ giao hàng không hợp lệ.");
            }

            current.setDefaultAddressId(addressId);
            return OperationResult.success("Đã đặt địa chỉ mặc định.");
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
        Map<String, User> users = getUsers(context);
        synchronized (users) {
            User current = users.get(username);
            if (current == null) {
                return OperationResult.fail("Không tìm thấy tài khoản.");
            }

            Address target = null;
            for (Address address : current.getShippingAddresses()) {
                if (address.getId().equals(addressId)) {
                    target = address;
                    break;
                }
            }

            if (target == null) {
                return OperationResult.fail("Địa chỉ giao hàng không hợp lệ.");
            }

            target.setRecipientName(recipientName);
            target.setRecipientPhone(recipientPhone);
            target.setShippingAddress(shippingAddress);

            if (setDefault) {
                current.setDefaultAddressId(addressId);
            }

            return OperationResult.success("Đã cập nhật địa chỉ giao hàng.");
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

    @SuppressWarnings("unchecked")
    private static Map<String, User> getUsers(ServletContext context) {
        synchronized (context) {
            Object value = context.getAttribute(USERS_KEY);
            if (value == null) {
                Map<String, User> users = new HashMap<>();
                users.put("admin", new User("admin", "Quản trị viên", "admin123"));
                users.put("demo", new User("demo", "Khách Demo", "123456"));
                context.setAttribute(USERS_KEY, users);
                return users;
            }
            return (Map<String, User>) value;
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

    public static final class User {
        private final String username;
        private String fullName;
        private final String password;
        private int age;
        private String gender;
        private String email;
        private String phone;
        private String baseAddress;
        private String defaultAddressId;
        private final List<Address> shippingAddresses;

        public User(String username, String fullName, String password) {
            this.username = username;
            this.fullName = fullName;
            this.password = password;
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
