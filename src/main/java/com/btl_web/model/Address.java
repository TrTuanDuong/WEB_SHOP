package com.btl_web.model;

public class Address {
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

    public Address(Address source) {
        this(source.getId(), source.getRecipientName(), source.getRecipientPhone(), source.getShippingAddress());
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