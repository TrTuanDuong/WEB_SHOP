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
import java.io.PrintWriter;

@WebServlet("/api/product")
public class ProductApiServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (!userDAO.isAdmin(currentUser)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }

        String productCode = request.getParameter("code");
        if (productCode == null || productCode.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Missing product code\"}");
            return;
        }

        ClothingStore.ClothingItem product = ClothingStore.findByCode(productCode);
        if (product == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\": \"Product not found\"}");
            return;
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print("{");
        out.print("\"productCode\": \"" + escapeJson(product.getProductCode()) + "\",");
        out.print("\"name\": \"" + escapeJson(product.getName()) + "\",");
        out.print("\"category\": \"" + escapeJson(product.getCategory()) + "\",");
        out.print("\"size\": \"" + escapeJson(product.getSize()) + "\",");
        out.print("\"color\": \"" + escapeJson(product.getColor()) + "\",");
        out.print("\"price\": " + product.getPrice().toPlainString() + ",");
        out.print("\"stockQuantity\": " + product.getStockQuantity());
        out.print("}");
        out.flush();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
