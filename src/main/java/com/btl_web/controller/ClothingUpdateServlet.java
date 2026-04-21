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

@WebServlet("/clothes/update")
public class ClothingUpdateServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (!userDAO.isAdmin(currentUser)) {
            session.setAttribute("authError", "Unauthorized");
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String productCode = request.getParameter("productCode");
        String name = request.getParameter("name");
        String category = request.getParameter("category");
        String size = request.getParameter("size");
        String color = request.getParameter("color");
        String priceStr = request.getParameter("price");
        String stockStr = request.getParameter("stockQuantity");

        if (productCode == null || productCode.isEmpty() || name == null || name.isEmpty()) {
            session.setAttribute("formError", "Required fields are missing");
            response.sendRedirect(request.getContextPath() + "/addproducts");
            return;
        }

        try {
            BigDecimal price = new BigDecimal(priceStr);
            int stock = Integer.parseInt(stockStr);
            ClothingStore.update(productCode, name, category, size, color, price, stock);
            session.setAttribute("successMessage", "Product updated successfully");
        } catch (Exception e) {
            session.setAttribute("formError", "Error updating product: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/addproducts");
    }
}
