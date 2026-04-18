package com.btl_web.controller;


import com.btl_web.dao.UserDAO;
import com.btl_web.model.User;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/addproducts")
public class AddProductsServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");
        if (!userDAO.isAdmin(currentUser)) {
            session.setAttribute("authError", "Chỉ tài khoản admin mới được truy cập trang quản trị sản phẩm.");
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        request.getRequestDispatcher("/addproducts.jsp").forward(request, response);
    }
}