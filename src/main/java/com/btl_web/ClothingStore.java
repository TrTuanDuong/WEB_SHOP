package com.btl_web;

import javax.servlet.ServletContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClothingStore {
    private static final String ITEMS_KEY = "clothingItems";

    private ClothingStore() {
    }

    public static boolean exists(ServletContext context, String productCode) {
        for (ClothingItem item : all(context)) {
            if (item.getProductCode().equalsIgnoreCase(productCode)) {
                return true;
            }
        }
        return false;
    }

    public static void insert(
            ServletContext context,
            String productCode,
            String name,
            String category,
            String size,
            String color,
            BigDecimal price,
            int stockQuantity) {
        List<ClothingItem> items = getMutableItems(context);
        items.add(new ClothingItem(productCode, name, category, size, color, price, stockQuantity));
    }

    public static List<ClothingItem> all(ServletContext context) {
        List<ClothingItem> items = getMutableItems(context);
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    @SuppressWarnings("unchecked")
    private static List<ClothingItem> getMutableItems(ServletContext context) {
        synchronized (context) {
            Object value = context.getAttribute(ITEMS_KEY);
            if (value == null) {
                List<ClothingItem> items = new ArrayList<>();
                context.setAttribute(ITEMS_KEY, items);
                return items;
            }
            return (List<ClothingItem>) value;
        }
    }

    public static final class ClothingItem {
        private final String productCode;
        private final String name;
        private final String category;
        private final String size;
        private final String color;
        private final BigDecimal price;
        private final int stockQuantity;

        public ClothingItem(
                String productCode,
                String name,
                String category,
                String size,
                String color,
                BigDecimal price,
                int stockQuantity) {
            this.productCode = productCode;
            this.name = name;
            this.category = category;
            this.size = size;
            this.color = color;
            this.price = price;
            this.stockQuantity = stockQuantity;
        }

        public String getProductCode() {
            return productCode;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
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

        public int getStockQuantity() {
            return stockQuantity;
        }
    }
}
