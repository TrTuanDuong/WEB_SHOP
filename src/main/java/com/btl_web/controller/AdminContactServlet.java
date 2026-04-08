package com.btl_web.controller;

import com.btl_web.model.UserStore;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@WebServlet(urlPatterns = { "/admin-contact", "/admin-contact/send" })
public class AdminContactServlet extends HttpServlet {
    private static final String REQUESTS_KEY = "adminContactRequests";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserStore.User currentUser = requireLogin(request, response);
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

        saveRequest(request, currentUser, topic, content);

        request.getSession().setAttribute("contactSuccess", "Yêu cầu đã được ghi nhận. Admin sẽ phản hồi sau.");
        response.sendRedirect(request.getContextPath() + "/admin-contact");
    }

    private void saveRequest(HttpServletRequest request, UserStore.User currentUser, String topic, String content) {
        List<ContactRequest> requests = getMutableRequests(request);
        synchronized (requests) {
            requests.add(0, new ContactRequest(
                    currentUser.getUsername(),
                    currentUser.getFullName(),
                    topic,
                    content,
                    LocalDateTime.now().format(TIME_FORMATTER)));
        }
    }

    private boolean isAdmin(UserStore.User currentUser) {
        return currentUser != null && "admin".equals(currentUser.getUsername());
    }

    @SuppressWarnings("unchecked")
    private List<ContactRequest> getMutableRequests(HttpServletRequest request) {
        synchronized (getServletContext()) {
            Object value = getServletContext().getAttribute(REQUESTS_KEY);
            if (value == null) {
                List<ContactRequest> requests = new ArrayList<>();
                getServletContext().setAttribute(REQUESTS_KEY, requests);
                return requests;
            }
            return (List<ContactRequest>) value;
        }
    }

    private List<ContactRequest> allRequests(HttpServletRequest request) {
        List<ContactRequest> requests = getMutableRequests(request);
        synchronized (requests) {
            return Collections.unmodifiableList(new ArrayList<>(requests));
        }
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
