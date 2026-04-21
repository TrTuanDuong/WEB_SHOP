/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.btl_web.model;

import java.math.BigDecimal;

/**
 *
 * @author ADMINN
 */
public class Product {
    private String id;
    private String name;
    private String group;
    private String segment;
    private String size;
    private String color;
    private BigDecimal price;
    private String image;
    public Product(String id, String name, String group, String segment, String size, String color, BigDecimal price, String image) {
        this.id = id;
        this.name = name;
        this.group = group;
        this.segment = segment;
        this.size = size;
        this.color = color;
        this.price = price;
        this.image = image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public String getSegment() {
        return segment;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
}
