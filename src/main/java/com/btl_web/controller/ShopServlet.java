package com.btl_web.controller;

import com.btl_web.dao.ProductDAO;
import com.btl_web.model.CartStore;
import com.btl_web.model.Product;
import com.btl_web.model.User;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/shop")
public class ShopServlet extends HttpServlet {
    private static final int PAGE_SIZE = 40;
    private ProductDAO productDAO = new ProductDAO();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String group = normalizeOrDefault(request.getParameter("group"), "all");
        String segment = normalizeOrDefault(request.getParameter("segment"), "all");
        String keyword = normalizeOrDefault(request.getParameter("q"), "");
        int page = parsePage(request.getParameter("page"));

        segment = sanitizeSegmentByGroup(group, segment);

        // Danh sach ket qua sau khi loc theo group, segment va tu khoa tim kiem.
        List<Product> filtered = new ArrayList<>();
        try {
            for (Product product : productDAO.all(getServletContext())) {
                if (!matchesGroup(product, group)) {
                    continue;
                }
                if (!matchesSegment(product, segment)) {
                    continue;
                }
                if (!matchesKeyword(product, keyword)) {
                    continue;
                }
                filtered.add(product);
            }
        } catch (SQLException ex) {
            System.getLogger(ShopServlet.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

        int totalItems = filtered.size();
        int totalPages = totalItems == 0 ? 1 : (int) Math.ceil((double) totalItems / PAGE_SIZE);
        if (page > totalPages) {
            page = totalPages;
        }

        int fromIndex = (page - 1) * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, totalItems);

        List<Product> pageItems;
        if (totalItems == 0) {
            pageItems = new ArrayList<>();
        } else {
            // Chi lay cac phan tu thuoc trang hien tai de hien thi.
            pageItems = filtered.subList(fromIndex, toIndex);
        }

        request.setAttribute("products", pageItems);
        request.setAttribute("group", group);
        request.setAttribute("segment", segment);
        request.setAttribute("q", keyword);
        request.setAttribute("page", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalItems", totalItems);

        HttpSession session = request.getSession(false);
        if (session != null) {
            User currentUser = (User) session.getAttribute("currentUser");
            if (currentUser != null) {
                request.setAttribute(
                        "cartCount",
                        CartStore.totalQuantityByUsername(getServletContext(), currentUser.getUsername()));
            }
        }

        request.getRequestDispatcher("/shop.jsp").forward(request, response);
    }

    private boolean matchesGroup(Product product, String group) {
        if ("all".equals(group)) {
            return true;
        }
        return product.getGroup().equalsIgnoreCase(group);
    }

    private boolean matchesSegment(Product product, String segment) {
        if ("all".equals(segment)) {
            return true;
        }
        return product.getSegment().equalsIgnoreCase(mapSegment(segment));
    }
    
    private boolean matchesKeyword(Product product, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        String normalized = keyword.toLowerCase();
        return product.getName().toLowerCase().contains(normalized)
                || product.getId().toLowerCase().contains(normalized);
    }

    private String mapSegment(String rawSegment) {
        switch (rawSegment) {
            case "male":
                return "Nam";
            case "female":
                return "Nữ";
            case "boy":
                return "Bé trai";
            case "girl":
                return "Bé gái";
            default:
                return rawSegment;
        }
    }

    private String sanitizeSegmentByGroup(String group, String segment) {
        if ("Người lớn".equals(group)) {
            if (!"all".equals(segment) && !"male".equals(segment) && !"female".equals(segment)) {
                return "all";
            }
            return segment;
        }

        if ("Trẻ em".equals(group)) {
            if (!"all".equals(segment) && !"boy".equals(segment) && !"girl".equals(segment)) {
                return "all";
            }
            return segment;
        }

        return segment;
    }

    private int parsePage(String rawPage) {
        try {
            int page = Integer.parseInt(rawPage);
            return Math.max(page, 1);
        } catch (Exception ignored) {
            return 1;
        }
    }

    private String normalizeOrDefault(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }
}
