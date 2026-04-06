package com.btl_web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = { "/admin-contact", "/admin-contact/send" })
public class AdminContactServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        requireLogin(request, response);
        if (response.isCommitted()) {
            return;
        }
        request.getRequestDispatcher("/admin-contact.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        UserStore.User currentUser = requireLogin(request, response);
        if (currentUser == null) {
            return;
        }

        String topic = normalize(request.getParameter("topic"));
        String content = normalize(request.getParameter("content"));
        if (topic.isEmpty() || content.isEmpty()) {
            request.getSession().setAttribute("contactError", "Vui lòng chọn yêu cầu và nhập nội dung.");
            response.sendRedirect(request.getContextPath() + "/admin-contact");
            return;
        }

        request.getSession().setAttribute("contactSuccess", "Yêu cầu đã được ghi nhận. Admin sẽ phản hồi sau.");
        response.sendRedirect(request.getContextPath() + "/admin-contact");
    }

    private UserStore.User requireLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();
        UserStore.User currentUser = (UserStore.User) session.getAttribute("currentUser");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return null;
        }
        return currentUser;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
