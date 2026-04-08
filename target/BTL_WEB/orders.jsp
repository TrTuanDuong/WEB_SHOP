<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.btl_web.model.OrderStore" %>
<%@ page import="com.btl_web.model.UserStore" %>
<%
    UserStore.User currentUser = (UserStore.User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/auth/login");
        return;
    }

    @SuppressWarnings("unchecked")
    List<OrderStore.Order> orders = (List<OrderStore.Order>) request.getAttribute("orders");
    if (orders == null) {
        orders = java.util.Collections.emptyList();
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đơn hàng</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700&family=Archivo+Black&display=swap" rel="stylesheet">
    <style>
        body {
            margin: 0;
            min-height: 100vh;
            font-family: "Plus Jakarta Sans", sans-serif;
            background: linear-gradient(180deg, #faf8f2 0%, #f3efe7 100%);
            padding: 12px 18px 24px;
            color: #22302f;
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
            box-shadow: 0 12px 30px rgba(35, 48, 46, 0.08);
            padding: 10px 12px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 10px;
            flex-wrap: wrap;
        }
        .logo { font-family: "Archivo Black", sans-serif; font-size: 1.05rem; }
        .links { display: flex; gap: 8px; flex-wrap: wrap; }
        .link-btn {
            display: inline-block; text-decoration: none; border: 1px solid #ced8d2; border-radius: 10px;
            padding: 8px 11px; color: #2b4442; background: #fff; font-weight: 600; font-size: 0.88rem;
        }
        .link-btn.primary {
            border-color: #0d6c63;
            background: #0d6c63;
            color: #fff;
        }

        .link-btn.logout {
            border-color: #6b1f24;
            background: #6b1f24;
            color: #fff;
        }
        .card {
            max-width: 1280px;
            margin: 0 auto;
            border: 1px solid #dbe4de;
            border-radius: 16px;
            background: #fffdf7;
            box-shadow: 0 16px 36px rgba(36, 47, 45, 0.1);
            padding: 18px;
        }
        .order {
            border: 1px solid #d9e2dc;
            border-radius: 14px;
            padding: 14px;
            background: #fff;
            margin-bottom: 14px;
        }
        .order-head {
            display: flex;
            justify-content: space-between;
            gap: 10px;
            flex-wrap: wrap;
            margin-bottom: 10px;
        }
        .title { font-weight: 700; font-size: 1rem; }
        .meta { color: #667976; font-size: 0.9rem; }
        .status {
            display: inline-block;
            border-radius: 999px;
            padding: 5px 10px;
            background: #e7f4f1;
            color: #0d6c63;
            font-weight: 700;
            font-size: 0.82rem;
        }
        table { width: 100%; border-collapse: collapse; margin-top: 8px; }
        th, td {
            border-bottom: 1px solid #e4ebe6;
            text-align: left;
            padding: 9px 8px;
            font-size: 0.92rem;
        }
        th {
            background: #f6f9f8;
            font-size: 0.78rem;
            text-transform: uppercase;
            letter-spacing: 0.06em;
            color: #4d6563;
        }
        .empty {
            padding: 24px;
            text-align: center;
            color: #667976;
        }
        .status-list {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
            margin-bottom: 12px;
        }
        .pill {
            border: 1px solid #d4ded8;
            background: #fff;
            border-radius: 999px;
            padding: 6px 10px;
            font-size: 0.82rem;
        }
    </style>
</head>
<body>
<div class="topbar">
    <div class="logo">Linen Lab | Đơn hàng</div>
    <div class="links">
        <a class="link-btn" href="<%= request.getContextPath() %>/cart">Giỏ hàng</a>
        <a class="link-btn" href="<%= request.getContextPath() %>/shop">Về shop</a>
        <a class="link-btn" href="<%= request.getContextPath() %>/profile">Trang cá nhân</a>
        <a class="link-btn logout" href="<%= request.getContextPath() %>/auth/logout">Đăng xuất</a>
    </div>
</div>

<div class="card">
    <h1>Đơn hàng của <%= currentUser.getFullName() %></h1>
    <div class="status-list">
        <span class="pill">Chờ xác nhận</span>
        <span class="pill">Đã xác nhận</span>
        <span class="pill">Đang giao</span>
        <span class="pill">Đã giao</span>
        <span class="pill">Đã huỷ</span>
        <span class="pill">Đã trả hàng</span>
    </div>

    <% if (orders.isEmpty()) { %>
        <div class="empty">Chưa có đơn hàng nào.</div>
    <% } else { %>
        <% for (OrderStore.Order order : orders) { %>
            <div class="order">
                <div class="order-head">
                    <div>
                        <div class="title">Mã đơn: <%= order.getId() %></div>
                        <div class="meta">Khách: <%= order.getCustomerName() %> | Ngày đặt: <%= order.getCreatedAt() %></div>
                        <div class="meta">Giao tới: <%= order.getShippingAddress() %></div>
                    </div>
                    <div>
                        <span class="status"><%= order.getStatus().getLabel() %></span>
                    </div>
                </div>

                <table>
                    <thead>
                    <tr>
                        <th>Mã SP</th>
                        <th>Tên SP</th>
                        <th>SL</th>
                        <th>Đơn giá</th>
                        <th>Thành tiền</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% for (OrderStore.OrderLine line : order.getLines()) { %>
                        <tr>
                            <td><%= line.getProductId() %></td>
                            <td><%= line.getProductName() %></td>
                            <td><%= line.getQuantity() %></td>
                            <td><%= line.getUnitPrice().toPlainString() %> VND</td>
                            <td><%= line.getLineTotal().toPlainString() %> VND</td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
                <div class="meta" style="margin-top: 10px; font-weight: 700;">Tổng đơn: <%= order.getTotal().toPlainString() %> VND</div>
            </div>
        <% } %>
    <% } %>
</div>
</body>
</html>
