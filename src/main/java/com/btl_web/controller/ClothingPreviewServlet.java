package com.btl_web.controller;

import com.btl_web.dao.UserDAO;
import com.btl_web.model.ClothingStore;
import com.btl_web.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/clothes/preview")
public class ClothingPreviewServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");
        if (!userDAO.isAdmin(currentUser)) {
            session.setAttribute("authError", "Chỉ tài khoản admin mới được thao tác sản phẩm.");
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        request.setCharacterEncoding("UTF-8");

        String productCode = normalize(request.getParameter("productCode"));
        String name = normalize(request.getParameter("name"));
        String category = normalize(request.getParameter("category"));
        String size = normalize(request.getParameter("size"));
        String color = normalize(request.getParameter("color"));
        String priceText = normalize(request.getParameter("price"));
        String stockText = normalize(request.getParameter("stockQuantity"));

        request.setAttribute("enteredProductCode", productCode);
        request.setAttribute("enteredName", name);
        request.setAttribute("enteredCategory", category);
        request.setAttribute("enteredSize", size);
        request.setAttribute("enteredColor", color);
        request.setAttribute("enteredPrice", priceText);
        request.setAttribute("enteredStockQuantity", stockText);

        session.setAttribute("lastCategory", category);

        BigDecimal price = null;
        Integer stockQuantity = null;
        boolean hasError = false;

        if (isBlank(productCode)) {
            request.setAttribute("errorProductCode", "Mã sản phẩm không được để trống.");
            hasError = true;
        }
        if (isBlank(name)) {
            request.setAttribute("errorName", "Tên sản phẩm không được để trống.");
            hasError = true;
        }
        if (isBlank(category)) {
            request.setAttribute("errorCategory", "Loại quần áo không được để trống.");
            hasError = true;
        }
        if (isBlank(size)) {
            request.setAttribute("errorSize", "Size không được để trống.");
            hasError = true;
        }
        if (isBlank(color)) {
            request.setAttribute("errorColor", "Màu sắc không được để trống.");
            hasError = true;
        }

        if (isBlank(priceText)) {
            request.setAttribute("errorPrice", "Giá bán không được để trống.");
            hasError = true;
        } else {
            try {
                price = new BigDecimal(priceText);
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    request.setAttribute("errorPrice", "Giá bán phải lớn hơn 0.");
                    hasError = true;
                }
            } catch (NumberFormatException ex) {
                request.setAttribute("errorPrice", "Giá bán không hợp lệ.");
                hasError = true;
            }
        }

        if (isBlank(stockText)) {
            request.setAttribute("errorStockQuantity", "Số lượng tồn không được để trống.");
            hasError = true;
        } else {
            try {
                stockQuantity = Integer.parseInt(stockText);
                if (stockQuantity < 0) {
                    request.setAttribute("errorStockQuantity", "Số lượng tồn không được âm.");
                    hasError = true;
                }
            } catch (NumberFormatException ex) {
                request.setAttribute("errorStockQuantity", "Số lượng tồn phải là số nguyên.");
                hasError = true;
            }
        }

        if (!isBlank(productCode)) {
            try {
                if (ClothingStore.exists(productCode)) {
                    request.setAttribute("errorProductCode", "Mã sản phẩm đã tồn tại.");
                    hasError = true;
                }
            } catch (IllegalStateException ex) {
                request.setAttribute("formError", "Không thể kết nối CSDL. Kiểm tra DB_URL/DB_USER/DB_PASSWORD.");
                hasError = true;
            }
        }

        if (hasError) {
            request.getRequestDispatcher("/addproducts.jsp").forward(request, response);
            return;
        }

        session.setAttribute("draftProductCode", productCode);
        session.setAttribute("draftName", name);
        session.setAttribute("draftCategory", category);
        session.setAttribute("draftSize", size);
        session.setAttribute("draftColor", color);
        session.setAttribute("draftPrice", price.toPlainString());
        session.setAttribute("draftStockQuantity", String.valueOf(stockQuantity));

        request.getRequestDispatcher("/confirm.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/addproducts");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
