package com.btl_web.controller;

import com.btl_web.model.ClothingStore;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/clothes/confirm")
public class ClothingConfirmServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        String productCode = normalize((String) session.getAttribute("draftProductCode"));
        String name = normalize((String) session.getAttribute("draftName"));
        String category = normalize((String) session.getAttribute("draftCategory"));
        String size = normalize((String) session.getAttribute("draftSize"));
        String color = normalize((String) session.getAttribute("draftColor"));
        String priceText = normalize((String) session.getAttribute("draftPrice"));
        String stockText = normalize((String) session.getAttribute("draftStockQuantity"));

        if (isBlank(productCode) || isBlank(name) || isBlank(category) || isBlank(size)
                || isBlank(color) || isBlank(priceText) || isBlank(stockText)) {
            session.setAttribute("formError", "Phiên nhập liệu không hợp lệ, vui lòng nhập lại.");
            response.sendRedirect(request.getContextPath() + "/addproducts");
            return;
        }

        BigDecimal price;
        int stockQuantity;
        try {
            price = new BigDecimal(priceText);
            stockQuantity = Integer.parseInt(stockText);
        } catch (NumberFormatException ex) {
            session.setAttribute("formError", "Dữ liệu giá hoặc số lượng không hợp lệ, vui lòng nhập lại.");
            response.sendRedirect(request.getContextPath() + "/addproducts");
            return;
        }

        try {
            if (ClothingStore.exists(productCode)) {
                session.setAttribute("errorProductCode", "Mã sản phẩm đã tồn tại.");
                session.setAttribute("enteredProductCode", productCode);
                session.setAttribute("enteredName", name);
                session.setAttribute("enteredCategory", category);
                session.setAttribute("enteredSize", size);
                session.setAttribute("enteredColor", color);
                session.setAttribute("enteredPrice", priceText);
                session.setAttribute("enteredStockQuantity", stockText);
                response.sendRedirect(request.getContextPath() + "/addproducts");
                return;
            }

            ClothingStore.insert(productCode, name, category, size, color, price, stockQuantity);
        } catch (IllegalStateException ex) {
            session.setAttribute("formError", "Không thể kết nối CSDL. Kiểm tra DB_URL/DB_USER/DB_PASSWORD.");
            response.sendRedirect(request.getContextPath() + "/addproducts");
            return;
        }

        session.setAttribute("lastCategory", category);
        session.setAttribute("successMessage", "Đã thêm sản phẩm quần áo thành công.");
        clearDraft(session);
        response.sendRedirect(request.getContextPath() + "/addproducts");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/addproducts");
    }

    private void clearDraft(HttpSession session) {
        session.removeAttribute("draftProductCode");
        session.removeAttribute("draftName");
        session.removeAttribute("draftCategory");
        session.removeAttribute("draftSize");
        session.removeAttribute("draftColor");
        session.removeAttribute("draftPrice");
        session.removeAttribute("draftStockQuantity");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
