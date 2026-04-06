package com.btl_web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/shop")
public class ShopServlet extends HttpServlet {
    private static final int PAGE_SIZE = 40;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String group = normalizeOrDefault(request.getParameter("group"), "all");
        String segment = normalizeOrDefault(request.getParameter("segment"), "all");
        String keyword = normalizeOrDefault(request.getParameter("q"), "");
        int page = parsePage(request.getParameter("page"));

        segment = sanitizeSegmentByGroup(group, segment);

        List<ShopCatalog.Product> filtered = new ArrayList<>();
        for (ShopCatalog.Product product : ShopCatalog.all(getServletContext())) {
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

        int totalItems = filtered.size();
        int totalPages = totalItems == 0 ? 1 : (int) Math.ceil((double) totalItems / PAGE_SIZE);
        if (page > totalPages) {
            page = totalPages;
        }

        int fromIndex = (page - 1) * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, totalItems);

        List<ShopCatalog.Product> pageItems;
        if (totalItems == 0) {
            pageItems = new ArrayList<>();
        } else {
            pageItems = filtered.subList(fromIndex, toIndex);
        }

        request.setAttribute("products", pageItems);
        request.setAttribute("group", group);
        request.setAttribute("segment", segment);
        request.setAttribute("q", keyword);
        request.setAttribute("page", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalItems", totalItems);

        request.getRequestDispatcher("/shop.jsp").forward(request, response);
    }

    private boolean matchesGroup(ShopCatalog.Product product, String group) {
        if ("all".equals(group)) {
            return true;
        }
        return product.getGroup().equalsIgnoreCase(group);
    }

    private boolean matchesSegment(ShopCatalog.Product product, String segment) {
        if ("all".equals(segment)) {
            return true;
        }
        return product.getSegment().equalsIgnoreCase(mapSegment(segment));
    }

    private boolean matchesKeyword(ShopCatalog.Product product, String keyword) {
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
