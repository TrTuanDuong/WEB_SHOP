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

@WebServlet("/clothes/delete")
public class ClothingDeleteServlet extends HttpServlet {
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
        if (productCode == null || productCode.isEmpty()) {
            session.setAttribute("formError", "Product code is required");
            response.sendRedirect(request.getContextPath() + "/addproducts");
            return;
        }

        try {
            ClothingStore.delete(productCode);
            session.setAttribute("successMessage", "Product deleted successfully");
        } catch (Exception e) {
            session.setAttribute("formError", "Error deleting product: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/addproducts");
    }
}
