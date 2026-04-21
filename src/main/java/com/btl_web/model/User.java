package com.btl_web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User {
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
    private String branchId;
    private String branchName;
    private String membershipTier;
    private java.math.BigDecimal membershipDiscountRate;
    private java.math.BigDecimal spendingLast6Months;
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
        this.branchId = "";
        this.branchName = "";
        this.membershipTier = "STANDARD";
        this.membershipDiscountRate = java.math.BigDecimal.ZERO;
        this.spendingLast6Months = java.math.BigDecimal.ZERO;
    }

    public User(User source) {
        this(
                source.getUsername(),
                source.getFullName(),
                source.getPassword(),
                source.getRoleCode(),
                source.getRoleName());
        this.age = source.getAge();
        this.gender = source.getGender();
        this.email = source.getEmail();
        this.phone = source.getPhone();
        this.baseAddress = source.getBaseAddress();
        this.defaultAddressId = source.getDefaultAddressId();
        this.branchId = source.getBranchId();
        this.branchName = source.getBranchName();
        this.membershipTier = source.getMembershipTier();
        this.membershipDiscountRate = source.getMembershipDiscountRate();
        this.spendingLast6Months = source.getSpendingLast6Months();
        for (Address address : source.getShippingAddresses()) {
            this.shippingAddresses.add(new Address(address));
        }
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

    public boolean isCompanyOwner() {
        return "COMPANY_OWNER".equalsIgnoreCase(roleCode) || isAdmin();
    }

    public boolean isBranchOwner() {
        return "BRANCH_OWNER".equalsIgnoreCase(roleCode);
    }

    public boolean isCustomer() {
        return "CUSTOMER".equalsIgnoreCase(roleCode);
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

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getMembershipTier() {
        return membershipTier;
    }

    public void setMembershipTier(String membershipTier) {
        this.membershipTier = membershipTier;
    }

    public java.math.BigDecimal getMembershipDiscountRate() {
        return membershipDiscountRate;
    }

    public void setMembershipDiscountRate(java.math.BigDecimal membershipDiscountRate) {
        this.membershipDiscountRate = membershipDiscountRate;
    }

    public java.math.BigDecimal getSpendingLast6Months() {
        return spendingLast6Months;
    }

    public void setSpendingLast6Months(java.math.BigDecimal spendingLast6Months) {
        this.spendingLast6Months = spendingLast6Months;
    }
}