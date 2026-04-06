package com.btl_web;

import javax.servlet.ServletContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ShopCatalog {
    private static final String PRODUCTS_KEY = "shopCatalogProducts";

    private ShopCatalog() {
    }

    public static List<Product> all(ServletContext context) {
        return Collections.unmodifiableList(getOrInit(context));
    }

    public static Product findById(ServletContext context, String id) {
        for (Product product : getOrInit(context)) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        return null;
    }

    private static List<Product> getOrInit(ServletContext context) {
        synchronized (context) {
            @SuppressWarnings("unchecked")
            List<Product> products = (List<Product>) context.getAttribute(PRODUCTS_KEY);
            if (products == null) {
                products = seedProducts();
                context.setAttribute(PRODUCTS_KEY, products);
            }
            return products;
        }
    }

    private static List<Product> seedProducts() {
        List<Product> products = new ArrayList<>();
        String[] adultMaleNames = { "Áo thun nam", "Áo sơ mi nam", "Quần jean nam", "Quần kaki nam", "Áo polo nam",
                "Áo khoác nam" };
        String[] adultFemaleNames = { "Áo thun nữ", "Áo kiểu nữ", "Váy nữ", "Quần jean nữ", "Áo khoác nữ", "Chân váy" };
        String[] kidBoyNames = { "Áo thun bé trai", "Quần short bé trai", "Áo sơ mi bé trai", "Set đồ bé trai",
                "Quần jean bé trai", "Áo khoác bé trai" };
        String[] kidGirlNames = { "Váy bé gái", "Áo kiểu bé gái", "Set đồ bé gái", "Quần legging bé gái",
                "Áo khoác bé gái", "Áo thun bé gái" };

        int counter = 1;
        counter = addGroup(products, counter, "AD-M", "Người lớn", "Nam", adultMaleNames, 18);
        counter = addGroup(products, counter, "AD-F", "Người lớn", "Nữ", adultFemaleNames, 18);
        counter = addGroup(products, counter, "KD-B", "Trẻ em", "Bé trai", kidBoyNames, 18);
        addGroup(products, counter, "KD-G", "Trẻ em", "Bé gái", kidGirlNames, 18);

        return products;
    }

    private static int addGroup(
            List<Product> products,
            int startIndex,
            String codePrefix,
            String group,
            String segment,
            String[] names,
            int count) {
        String[] colors = { "Đen", "Trắng", "Xanh", "Be", "Nâu", "Hồng" };
        String[] sizes = { "S", "M", "L", "XL" };
        int index = startIndex;

        for (int i = 0; i < count; i++) {
            String code = String.format("%s-%03d", codePrefix, index);
            String name = names[i % names.length] + " " + (i + 1);
            String size = sizes[i % sizes.length];
            String color = colors[i % colors.length];
            BigDecimal price = BigDecimal.valueOf(179000L + ((long) (i % 9) * 35000L));

            products.add(new Product(code, name, group, segment, size, color, price));
            index++;
        }

        return index;
    }

    public static final class Product {
        private final String id;
        private final String name;
        private final String group;
        private final String segment;
        private final String size;
        private final String color;
        private final BigDecimal price;

        public Product(String id, String name, String group, String segment, String size, String color,
                BigDecimal price) {
            this.id = id;
            this.name = name;
            this.group = group;
            this.segment = segment;
            this.size = size;
            this.color = color;
            this.price = price;
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
    }
}
