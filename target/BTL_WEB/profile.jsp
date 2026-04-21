<%@page import="com.btl_web.dao.UserDAO"%>
<%@page import="com.btl_web.model.User"%>
<%@page import="com.btl_web.model.Address"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/auth/login");
        return;
    }
    UserDAO userDAO = new UserDAO();
    User profileUser = (User) request.getAttribute("profileUser");
    if (profileUser == null) {
        profileUser = currentUser;
    }

    List<Address> addresses = profileUser.getShippingAddressesView();
    String profileError = (String) session.getAttribute("profileError");
    String profileSuccess = (String) session.getAttribute("profileSuccess");
    boolean profileLocked = userDAO.isFixedProfileLocked(profileUser);
    String membershipTierLabel = "Vô hạng";
    String profileTier = profileUser.getMembershipTier() == null ? "" : profileUser.getMembershipTier().trim();
    if (!profileTier.isEmpty() && !"STANDARD".equalsIgnoreCase(profileTier)) {
        membershipTierLabel = profileTier;
    }
    session.removeAttribute("profileError");
    session.removeAttribute("profileSuccess");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trang cá nhân</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Archivo+Black&display=swap" rel="stylesheet">
    <style>
        :root {
            --panel: #fffdf7;
            --text: #22302f;
            --muted: #667976;
            --line: #dbe4de;
            --accent: #0d6c63;
            --accent-soft: #e7f4f1;
            --danger: #b42318;
            --radius: 14px;
            --shadow: 0 12px 30px rgba(35, 48, 46, 0.08);
        }

        * { box-sizing: border-box; }

        body {
            margin: 0;
            font-family: "Plus Jakarta Sans", sans-serif;
            color: var(--text);
            background: linear-gradient(180deg, #faf8f2 0%, #f3efe7 100%);
            padding: 12px 18px 24px;
        }

        .topbar {
            max-width: 1280px;
            margin: 0 auto 14px;
            position: sticky;
            top: 10px;
            z-index: 20;
            border: 1px solid #d6e0da;
            background: rgba(255, 253, 247, 0.92);
            backdrop-filter: blur(6px);
            border-radius: 14px;
            box-shadow: var(--shadow);
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

        .link-btn.primary {
            border-color: var(--accent);
            background: var(--accent);
            color: #fff;
        }

        .link-btn.logout {
            border-color: #6b1f24;
            background: #6b1f24;
            color: #fff;
        }

        .shell {
            max-width: 1280px;
            margin: 0 auto;
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 14px;
        }

        .card {
            border: 1px solid var(--line);
            border-radius: var(--radius);
            background: var(--panel);
            box-shadow: var(--shadow);
            padding: 14px;
        }

        h2 {
            margin: 0 0 10px;
            font-size: 1rem;
        }

        .desc {
            margin: 0 0 10px;
            color: var(--muted);
            font-size: 0.9rem;
        }

        .field { margin-bottom: 9px; }

        .field label {
            display: block;
            font-size: 0.8rem;
            margin-bottom: 4px;
            color: #4d6664;
            text-transform: uppercase;
            letter-spacing: 0.06em;
            font-weight: 700;
        }

        .field input,
        .field select,
        .field textarea {
            width: 100%;
            border: 1px solid #d1dbd5;
            border-radius: 10px;
            padding: 9px;
            font: inherit;
            resize: vertical;
            min-height: 40px;
            background: #fff;
        }

        .address-form,
        .address-item form {
            display: grid;
            gap: 10px;
        }

        .address-form .field,
        .address-item .field {
            margin-bottom: 0;
        }

        .address-form .form-actions {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            flex-wrap: wrap;
        }

        .address-form .check {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            font-size: 0.8rem;
            color: #4d6664;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.06em;
        }

        .address-form .check input,
        .address-item .inline-actions input {
            width: 16px;
            height: 16px;
            margin: 0;
            flex: 0 0 auto;
        }

        .field input[readonly],
        .field textarea[readonly] {
            background: #f4f7f5;
        }

        .locked-note {
            margin-bottom: 12px;
            padding: 10px;
            border-radius: 10px;
            background: #fff8e9;
            color: #8c5b10;
            border: 1px solid #f0d8a8;
            font-size: 0.9rem;
        }

        .btn {
            border: 0;
            border-radius: 10px;
            padding: 9px 12px;
            font: inherit;
            font-weight: 700;
            cursor: pointer;
        }

        .btn-primary { background: var(--accent); color: #fff; }
        .btn-soft { background: #edf1ef; color: #385452; }

        .msg {
            max-width: 1280px;
            margin: 0 auto 10px;
            border-radius: 10px;
            padding: 10px 12px;
            border: 1px solid;
            font-size: 0.9rem;
        }

        .msg.err { background: #fff3f2; color: var(--danger); border-color: #f0c8c3; }
        .msg.ok { background: #ecfaf6; color: #0d5f57; border-color: #b7e1d8; }

        .address-list {
            margin-top: 12px;
            display: grid;
            gap: 8px;
        }

        .address-item {
            border: 1px solid #d4ded8;
            border-radius: 10px;
            padding: 10px;
            background: #fff;
            display: grid;
            gap: 8px;
        }

        .address-item form {
            gap: 8px;
        }

        .address-item .name {
            font-weight: 700;
            margin-bottom: 4px;
        }

        .address-item .meta {
            color: var(--muted);
            font-size: 0.86rem;
        }

        .pill {
            display: inline-block;
            border-radius: 999px;
            background: var(--accent-soft);
            color: var(--accent);
            font-size: 0.78rem;
            font-weight: 700;
            padding: 4px 8px;
            margin-left: 6px;
        }

        .inline-actions {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 8px;
            flex-wrap: wrap;
        }

        @media (max-width: 980px) {
            .shell { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<div class="topbar">
    <div class="links">
        <a class="link-btn primary" href="<%= request.getContextPath() %>/admin-contact">Liên hệ admin</a>
    </div>
    <div class="logo">Linen Lab | Trang cá nhân</div>
    <div class="links">
        <% if (currentUser.isCompanyOwner()) { %>
            <a class="link-btn" href="<%= request.getContextPath() %>/company/dashboard">Dashboard công ty</a>
        <% } else if (currentUser.isBranchOwner()) { %>
            <a class="link-btn" href="<%= request.getContextPath() %>/branch/dashboard">Dashboard chi nhánh</a>
        <% } else { %>
            <a class="link-btn" href="<%= request.getContextPath() %>/profile">Hạng: <%= membershipTierLabel %></a>
        <% } %>
        <a class="link-btn" href="<%= request.getContextPath() %>/shop">Về shop</a>
        <a class="link-btn" href="<%= request.getContextPath() %>/cart">Giỏ hàng</a>
        <a class="link-btn" href="<%= request.getContextPath() %>/orders">Đơn hàng</a>
        <a class="link-btn logout" href="<%= request.getContextPath() %>/auth/logout">Đăng xuất</a>
    </div>
</div>

<% if (profileError != null) { %><div class="msg err"><%= profileError %></div><% } %>
<% if (profileSuccess != null) { %><div class="msg ok"><%= profileSuccess %></div><% } %>

<div class="shell">
    <section class="card">
        <h2>Hạng thành viên</h2>
        <p class="desc">
            Hạng hiện tại: <strong><%= membershipTierLabel %></strong> |
            Chi tiêu 6 tháng: <strong><%= profileUser.getSpendingLast6Months().toPlainString() %> VND</strong> |
            Ưu đãi hiện tại: <strong><%= profileUser.getMembershipDiscountRate().multiply(new java.math.BigDecimal("100")).stripTrailingZeros().toPlainString() %>%</strong>
        </p>

        <h2>Thông tin cá nhân cố định</h2>
        <p class="desc">Bắt buộc trước khi đặt hàng. Email và số điện thoại là duy nhất giữa các tài khoản.</p>

        <% if (profileLocked) { %>
            <div class="locked-note">Thông tin cá nhân đã được xác lập. Nếu muốn sửa, vui lòng liên hệ admin.</div>
        <% } %>

        <form action="<%= request.getContextPath() %>/profile/update" method="post">
            <div class="field">
                <label for="fullName">Họ và tên</label>
                <input id="fullName" name="fullName" value="<%= profileUser.getFullName() == null ? "" : profileUser.getFullName() %>" <%= profileLocked ? "readonly" : "" %>>
            </div>
            <div class="field">
                <label for="age">Tuổi</label>
                <input id="age" name="age" type="number" min="1" max="120" value="<%= profileUser.getAge() <= 0 ? "" : profileUser.getAge() %>" <%= profileLocked ? "readonly" : "" %>>
            </div>
            <div class="field">
                <label for="gender">Giới tính</label>
                <select id="gender" name="gender" <%= profileLocked ? "disabled" : "" %>>
                    <option value="">Chọn giới tính</option>
                    <option value="Nam" <%= "Nam".equals(profileUser.getGender()) ? "selected" : "" %>>Nam</option>
                    <option value="Nữ" <%= "Nữ".equals(profileUser.getGender()) ? "selected" : "" %>>Nữ</option>
                    <option value="Khác" <%= "Khác".equals(profileUser.getGender()) ? "selected" : "" %>>Khác</option>
                </select>
            </div>
            <div class="field">
                <label for="email">Email</label>
                <input id="email" name="email" type="email" value="<%= profileUser.getEmail() == null ? "" : profileUser.getEmail() %>" <%= profileLocked ? "readonly" : "" %>>
            </div>
            <div class="field">
                <label for="phone">Số điện thoại</label>
                <input id="phone" name="phone" value="<%= profileUser.getPhone() == null ? "" : profileUser.getPhone() %>" <%= profileLocked ? "readonly" : "" %>>
            </div>
            <div class="field">
                <label for="baseAddress">Địa chỉ cá nhân</label>
                <textarea id="baseAddress" name="baseAddress" <%= profileLocked ? "readonly" : "" %>><%= profileUser.getBaseAddress() == null ? "" : profileUser.getBaseAddress() %></textarea>
            </div>
            <% if (!profileLocked) { %>
                <button class="btn btn-primary" type="submit">Lưu thông tin cá nhân</button>
            <% } %>
        </form>
    </section>

    <section class="card">
        <h2>Địa chỉ giao hàng</h2>
        <p class="desc">Bạn có thể thêm nhiều địa chỉ. Hãy chọn 1 địa chỉ mặc định để đặt hàng nhanh.</p>

        <form class="address-form" action="<%= request.getContextPath() %>/profile/address/add" method="post">
            <div class="field">
                <label for="recipientName">Tên người nhận</label>
                <input id="recipientName" name="recipientName">
            </div>
            <div class="field">
                <label for="recipientPhone">Số điện thoại người nhận</label>
                <input id="recipientPhone" name="recipientPhone">
            </div>
            <div class="field">
                <label for="shippingAddress">Địa chỉ giao hàng</label>
                <textarea id="shippingAddress" name="shippingAddress"></textarea>
            </div>
            <div class="form-actions">
                <label class="check"><input type="checkbox" name="setDefault"> Đặt làm mặc định</label>
                <button class="btn btn-primary" type="submit">Thêm địa chỉ giao hàng</button>
            </div>
        </form>

        <div class="address-list">
            <% if (addresses.isEmpty()) { %>
                <div class="address-item">Chưa có địa chỉ giao hàng.</div>
            <% } else { %>
                <% for (Address address : addresses) { %>
                    <div class="address-item">
                        <form action="<%= request.getContextPath() %>/profile/address/update" method="post">
                            <input type="hidden" name="addressId" value="<%= address.getId() %>">
                            <div class="field">
                                <label>Tên người nhận</label>
                                <input name="recipientName" value="<%= address.getRecipientName() %>">
                            </div>
                            <div class="field">
                                <label>Số điện thoại người nhận</label>
                                <input name="recipientPhone" value="<%= address.getRecipientPhone() %>">
                            </div>
                            <div class="field">
                                <label>Địa chỉ giao hàng</label>
                                <textarea name="shippingAddress"><%= address.getShippingAddress() %></textarea>
                            </div>
                            <div class="name">
                                <%= address.getRecipientName() %>
                                <% if (address.getId().equals(profileUser.getDefaultAddressId())) { %>
                                    <span class="pill">Mặc định</span>
                                <% } %>
                            </div>
                            <div class="meta">Có thể sửa trực tiếp nội dung trên rồi bấm lưu.</div>
                            <div class="inline-actions">
                                <label><input type="checkbox" name="setDefault" <%= address.getId().equals(profileUser.getDefaultAddressId()) ? "checked" : "" %>> Đặt làm mặc định</label>
                                <button class="btn btn-soft" type="submit">Lưu địa chỉ</button>
                            </div>
                        </form>
                    </div>
                <% } %>
            <% } %>
        </div>
    </section>
</div>
</body>
</html>
