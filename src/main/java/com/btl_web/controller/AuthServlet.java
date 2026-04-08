package com.btl_web.controller;

import com.btl_web.model.UserStore;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = { "/auth/login", "/auth/register", "/auth/logout" })
public class AuthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/auth/logout".equals(path)) {
            HttpSession session = request.getSession();
            session.removeAttribute("currentUser");
            session.setAttribute("authSuccess", "Đã đăng xuất.");
            response.sendRedirect(request.getContextPath() + "/shop");
            return;
        }

        if ("/auth/login".equals(path)) {
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();
        if ("/auth/login".equals(path)) {
            handleLogin(request, response);
            return;
        }
        if ("/auth/register".equals(path)) {
            handleRegister(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/shop");
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String username = normalize(request.getParameter("username"));
        String password = normalize(request.getParameter("password"));

        request.setAttribute("enteredUsername", username);

        if (username.isEmpty() || password.isEmpty()) {
            request.setAttribute("authError", "Vui lòng nhập đầy đủ tài khoản và mật khẩu.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        UserStore.User user = UserStore.login(getServletContext(), username, password);
        if (user == null) {
            request.setAttribute("authError", "Sai tài khoản hoặc mật khẩu.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("currentUser", user);
        session.setAttribute("authSuccess", "Đăng nhập thành công. Bắt đầu mua sắm nhé!");
        response.sendRedirect(request.getContextPath() + "/shop");
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String fullName = normalize(request.getParameter("fullName"));
        String username = normalize(request.getParameter("username"));
        String password = normalize(request.getParameter("password"));
        String confirmPassword = normalize(request.getParameter("confirmPassword"));

        request.setAttribute("enteredFullName", fullName);
        request.setAttribute("enteredUsername", username);

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            request.setAttribute("authError", "Vui lòng điền đầy đủ thông tin đăng ký.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (username.length() < 4) {
            request.setAttribute("authError", "Tên đăng nhập phải từ 4 ký tự trở lên.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (password.length() < 6) {
            request.setAttribute("authError", "Mật khẩu phải từ 6 ký tự trở lên.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("authError", "Mật khẩu nhập lại không khớp.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        boolean registered = UserStore.register(getServletContext(), username, fullName, password);
        if (!registered) {
            request.setAttribute("authError", "Tên đăng nhập đã tồn tại.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("authSuccess", "Đăng ký thành công. Vui lòng đăng nhập để mua hàng.");
        response.sendRedirect(request.getContextPath() + "/auth/login");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
