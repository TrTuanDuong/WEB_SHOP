package com.btl_web.controller;

import com.btl_web.dao.UserDAO;
import com.btl_web.model.DbSupport;
import com.btl_web.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@WebServlet(urlPatterns = { "/admin-contact", "/admin-contact/send" })
public class AdminContactServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = requireLogin(request, response);
        if (response.isCommitted()) {
            return;
        }

        if (isAdmin(currentUser)) {
            request.setAttribute("adminRequests", allRequests(request));
        }

        request.getRequestDispatcher("/admin-contact.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        User currentUser = requireLogin(request, response);
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

        saveRequest(request, currentUser, topic, content);

        request.getSession().setAttribute("contactSuccess", "Yêu cầu đã được ghi nhận. Admin sẽ phản hồi sau.");
        response.sendRedirect(request.getContextPath() + "/admin-contact");
    }

    private void saveRequest(HttpServletRequest request, User currentUser, String topic, String content) {
        String sql = "INSERT INTO contact_requests (username, full_name, topic, content) VALUES (?, ?, ?, ?)";
        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, currentUser.getUsername());
            statement.setString(2, currentUser.getFullName());
            statement.setString(3, topic);
            statement.setString(4, content);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lưu yêu cầu liên hệ vào CSDL.", e);
        }
    }

    private boolean isAdmin(User currentUser) {
        return userDAO.isAdmin(currentUser);
    }

    private List<ContactRequest> allRequests(HttpServletRequest request) {
        String sql = "SELECT username, full_name, topic, content, created_at "
                + "FROM contact_requests ORDER BY created_at DESC";
        List<ContactRequest> requests = new ArrayList<>();
        try (Connection connection = DbSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Timestamp createdAtValue = resultSet.getTimestamp("created_at");
                String createdAt = createdAtValue == null
                        ? ""
                        : createdAtValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                                .format(TIME_FORMATTER);
                requests.add(new ContactRequest(
                        resultSet.getString("username"),
                        resultSet.getString("full_name"),
                        resultSet.getString("topic"),
                        resultSet.getString("content"),
                        createdAt));
            }
            return Collections.unmodifiableList(requests);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải yêu cầu liên hệ từ CSDL.", e);
        }
    }

    private User requireLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return null;
        }
        return currentUser;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public static final class ContactRequest {
        private final String username;
        private final String fullName;
        private final String topic;
        private final String content;
        private final String createdAt;

        public ContactRequest(String username, String fullName, String topic, String content, String createdAt) {
            this.username = username;
            this.fullName = fullName;
            this.topic = topic;
            this.content = content;
            this.createdAt = createdAt;
        }

        public String getUsername() {
            return username;
        }

        public String getFullName() {
            return fullName;
        }

        public String getTopic() {
            return topic;
        }

        public String getContent() {
            return content;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }
}
