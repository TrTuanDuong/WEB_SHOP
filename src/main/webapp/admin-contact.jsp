<%@page import="com.btl_web.model.User"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.btl_web.controller.AdminContactServlet" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/auth/login");
        return;
    }

    boolean adminView = currentUser.isAdmin();

    String contactError = (String) session.getAttribute("contactError");
    String contactSuccess = (String) session.getAttribute("contactSuccess");
    session.removeAttribute("contactError");
    session.removeAttribute("contactSuccess");

    @SuppressWarnings(
            
    
    "unchecked")
    List<AdminContactServlet.ContactRequest> adminRequests
            = (List<AdminContactServlet.ContactRequest>) request.getAttribute("adminRequests");
    if (adminRequests == null) {
        adminRequests = java.util.Collections.emptyList();
    }
%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title><%= adminView ? "Yêu cầu người dùng" : "Liên hệ admin"%></title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Archivo+Black&display=swap" rel="stylesheet">
        <style>
            *, *::before, *::after {
                box-sizing: border-box;
            }
            body {
                margin: 0;
                min-height: 100vh;
                font-family: "Plus Jakarta Sans", sans-serif;
                background: linear-gradient(180deg, #faf8f2 0%, #f3efe7 100%);
                padding: 12px 18px 24px;
                color: #22302f;
            }
            .topbar {
                max-width: 780px;
                margin: 0 auto 14px;
                position: sticky;
                top: 10px;
                z-index: 20;
                border: 1px solid #d6e0da;
                background: rgba(255, 253, 247, 0.92);
                backdrop-filter: blur(6px);
                border-radius: 14px;
                box-shadow: 0 12px 30px rgba(35, 48, 46, 0.08);
                padding: 10px 12px;
                display: flex;
                justify-content: space-between;
                align-items: center;
                gap: 10px;
                flex-wrap: wrap;
            }

            .logo {
                font-family: "Plus Jakarta Sans", sans-serif;
                font-size: 1.2rem;
                font-weight: 700;
                letter-spacing: -0.02em;
            }

            .links {
                display: flex;
                gap: 8px;
                flex-wrap: wrap;
            }
            .link-btn {
                display: inline-block;
                text-decoration: none;
                border: 1px solid #ced8d2;
                border-radius: 10px;
                padding: 8px 11px;
                color: #2b4442;
                background: #fff;
                font-weight: 600;
                font-size: 0.88rem;
            }
            .card {
                max-width: 780px;
                margin: 0 auto;
                border: 1px solid #dbe4de;
                border-radius: 16px;
                background: #fffdf7;
                box-shadow: 0 16px 36px rgba(36, 47, 45, 0.1);
                padding: 18px;
            }
            h1 {
                margin: 0 0 8px;
            }
            p {
                margin: 0 0 14px;
                color: #667976;
            }
            .field {
                margin-bottom: 10px;
            }
            label {
                display: block;
                margin-bottom: 5px;
                font-size: 0.86rem;
                font-weight: 700;
            }
            input, select, textarea {
                width: 100%;
                border: 1px solid #d1dbd5;
                border-radius: 10px;
                padding: 10px;
                font: inherit;
                background: #fff;
                max-width: 100%;
            }
            textarea {
                min-height: 120px;
                resize: vertical;
            }
            .btn {
                width: 100%;
                border: 0;
                border-radius: 10px;
                padding: 10px;
                font: inherit;
                font-weight: 700;
                background: #0d6c63;
                color: #fff;
                cursor: pointer;
            }
            .msg {
                margin-bottom: 10px;
                padding: 9px;
                border-radius: 8px;
                font-size: 0.9rem;
            }
            .err {
                background: #fff3f2;
                color: #b42318;
                border: 1px solid #f0c6c1;
            }
            .ok {
                background: #ecfaf6;
                color: #0d5f57;
                border: 1px solid #b7e1d8;
            }
            .request-list {
                display: grid;
                gap: 10px;
            }
            .request-item {
                border: 1px solid #dbe4de;
                border-radius: 12px;
                background: #fff;
                padding: 12px;
            }
            .request-meta {
                display: flex;
                justify-content: space-between;
                gap: 10px;
                flex-wrap: wrap;
                margin-bottom: 6px;
                font-size: 0.84rem;
                color: #5f7270;
            }
            .request-topic {
                font-weight: 700;
                margin: 0 0 6px;
            }
            .request-content {
                margin: 0;
                color: #2a3d3b;
                white-space: pre-wrap;
            }
            .empty {
                padding: 12px;
                border: 1px dashed #d4dfd9;
                border-radius: 10px;
                color: #5f7270;
            }
        </style>
    </head>
    <body>
        <div class="topbar">
            <div class="logo">Linen Lab | <%= adminView ? "Yêu cầu" : "Liên hệ admin"%></div>
            <div class="links">
                <a class="link-btn" href="<%= request.getContextPath()%>/profile">Trang cá nhân</a>
                <a class="link-btn" href="<%= request.getContextPath()%>/shop">Về shop</a>
            </div>
        </div>

        <div class="card">
            <% if (adminView) { %>
            <h1>Yêu cầu người dùng</h1>
            <p>Trang này chỉ hiển thị các yêu cầu đã gửi từ người dùng.</p>

            <% if (adminRequests.isEmpty()) { %>
            <div class="empty">Chưa có yêu cầu nào từ người dùng.</div>
            <% } else { %>
            <div class="request-list">
                <% for (AdminContactServlet.ContactRequest item : adminRequests) {%>
                <article class="request-item">
                    <div class="request-meta">
                        <span><strong><%= item.getFullName()%></strong> (@<%= item.getUsername()%>)</span>
                        <span><%= item.getCreatedAt()%></span>
                    </div>
                    <p class="request-topic"><%= item.getTopic()%></p>
                    <p class="request-content"><%= item.getContent()%></p>
                </article>
                <% } %>
            </div>
            <% } %>
            <% } else { %>
            <h1>Liên hệ admin</h1>
            <p>Chọn yêu cầu cần hỗ trợ, hệ thống sẽ ghi nhận để admin xử lý sau.</p>

            <% if (contactError != null) {%><div class="msg err"><%= contactError%></div><% } %>
            <% if (contactSuccess != null) {%><div class="msg ok"><%= contactSuccess%></div><% }%>

            <form action="<%= request.getContextPath()%>/admin-contact/send" method="post">
                <div class="field">
                    <label for="topic">Yêu cầu</label>
                    <select id="topic" name="topic">
                        <option value="">Chọn một yêu cầu</option>
                        <option value="Sửa thông tin cá nhân cố định">Sửa thông tin cá nhân cố định</option>
                        <option value="Sửa địa chỉ giao hàng">Sửa địa chỉ giao hàng</option>
                        <option value="Hỗ trợ đơn hàng">Hỗ trợ đơn hàng</option>
                        <option value="Khác">Khác</option>
                    </select>
                </div>
                <div class="field">
                    <label for="content">Nội dung</label>
                    <textarea id="content" name="content" placeholder="Mô tả chi tiết vấn đề hoặc yêu cầu của bạn"></textarea>
                </div>
                <button class="btn" type="submit">Gửi yêu cầu</button>
            </form>
            <% }%>
        </div>
    </body>
</html>
