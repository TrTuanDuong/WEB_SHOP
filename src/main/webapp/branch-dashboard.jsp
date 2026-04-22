<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.btl_web.dao.BusinessReportDAO" %>
<%@ page import="com.btl_web.model.OrderStore" %>
<%@ page import="com.btl_web.model.User" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/auth/login");
        return;
    }

    BusinessReportDAO.BranchStat stat = (BusinessReportDAO.BranchStat) request.getAttribute("branchStat");
    String branchError = (String) request.getAttribute("branchError");
    String branchSuccess = (String) request.getAttribute("branchSuccess");
    Integer pendingOrderCountObj = (Integer) request.getAttribute("pendingOrderCount");
    int pendingOrderCount = pendingOrderCountObj == null ? 0 : pendingOrderCountObj;
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
    <title>Dashboard Chủ Chi Nhánh</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        body {
            font-family: "Plus Jakarta Sans", sans-serif;
            background: linear-gradient(180deg, #faf8f2 0%, #f1ece2 100%);
            margin: 0;
            padding: 12px 16px 24px;
            color: #22302f;
        }
        .wrap { max-width: 1200px; margin: 0 auto; }
        .topbar {
            position: sticky;
            top: 10px;
            z-index: 20;
            border: 1px solid #d5e0da;
            background: rgba(255, 253, 247, 0.92);
            backdrop-filter: blur(6px);
            border-radius: 14px;
            box-shadow: 0 10px 28px rgba(35, 48, 46, 0.08);
            padding: 10px 12px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 10px;
            flex-wrap: wrap;
            margin-bottom: 14px;
        }
        .logo { font-size: 1.12rem; font-weight: 700; }
        .links { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
        .badge {
            display: inline-block;
            border-radius: 999px;
            padding: 6px 10px;
            border: 1px solid #c9d7d1;
            background: #f2f8f6;
            color: #33514e;
            font-size: 0.8rem;
            font-weight: 700;
        }
        .btn {
            text-decoration: none;
            padding: 8px 12px;
            border-radius: 10px;
            border: 1px solid #cfd8d3;
            background: #fff;
            color: #234;
            font-weight: 600;
            font-size: 0.88rem;
        }
        .grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; margin-bottom: 14px; }
        .card { background: #fffdf7; border: 1px solid #dbe4de; border-radius: 14px; padding: 14px; }
        .k { font-size: 0.82rem; color: #607270; text-transform: uppercase; letter-spacing: 0.06em; }
        .v { font-size: 1.2rem; font-weight: 700; }
        table { width: 100%; border-collapse: collapse; background: #fff; border: 1px solid #dbe4de; border-radius: 14px; overflow: hidden; }
        th, td { padding: 10px; border-bottom: 1px solid #e7eee9; text-align: left; }
        th { background: #f3f7f5; font-size: 0.82rem; }
        .err { padding: 12px; border-radius: 10px; background: #fff1ef; border: 1px solid #f2c8c2; color: #9e2a1d; }
        .ok { padding: 12px; border-radius: 10px; background: #ecfaf6; border: 1px solid #b7e1d8; color: #0f6159; margin-bottom: 10px; }
        .notice { padding: 12px; border-radius: 10px; background: #f4f8ff; border: 1px solid #d4def0; color: #2c4d7a; margin-bottom: 10px; }
        .action-btn {
            border: 0;
            border-radius: 8px;
            padding: 7px 10px;
            font: inherit;
            font-weight: 600;
            cursor: pointer;
            background: #0d6c63;
            color: #fff;
        }
        .status-pill {
            display: inline-block;
            border: 1px solid #d4ded8;
            border-radius: 999px;
            padding: 4px 9px;
            font-size: 0.82rem;
            font-weight: 600;
            background: #fff;
            color: #33514e;
        }
        @media (max-width: 900px) { .grid { grid-template-columns: 1fr; } }
    </style>
</head>
<body>
<div class="wrap">
    <div class="topbar">
        <div class="logo">Linen Lab | Dashboard chi nhánh</div>
        <div class="links">
            <span class="badge">Xin chào, <%= currentUser.getFullName() %></span>
            <a class="btn" href="<%= request.getContextPath() %>/shop">Về shop</a>
            <a class="btn" href="<%= request.getContextPath() %>/orders">Đơn hàng</a>
            <a class="btn" href="<%= request.getContextPath() %>/profile">Trang cá nhân</a>
            <a class="btn" href="<%= request.getContextPath() %>/auth/logout">Đăng xuất</a>
        </div>
    </div>

    <% if (branchError != null) { %>
        <div class="err"><%= branchError %></div>
    <% } %>
    <% if (branchSuccess != null) { %>
        <div class="ok"><%= branchSuccess %></div>
    <% } %>
    <% if (stat != null) { %>
        <div class="grid">
            <div class="card"><div class="k">Chi nhánh</div><div class="v"><%= stat.getBranchName() %></div></div>
            <div class="card"><div class="k">Đơn hàng</div><div class="v"><%= stat.getOrderCount() %></div></div>
            <div class="card"><div class="k">Doanh số</div><div class="v"><%= stat.getRevenue().toPlainString() %> VND</div></div>
            <div class="card"><div class="k">Tồn kho</div><div class="v"><%= stat.getTotalInventory() %></div></div>
        </div>

        <div class="notice">
            Thông báo: hiện có <strong><%= pendingOrderCount %></strong> đơn hàng đang chờ xác nhận.
        </div>

        <div class="card" style="margin-bottom: 12px;">
            <div class="k">Địa chỉ chi nhánh</div>
            <div class="v" style="font-size: 1rem;"><%= stat.getBranchAddress() %></div>
        </div>

        <table>
            <thead>
            <tr>
                <th>Mã đơn</th>
                <th>Khách</th>
                <th>Ngày đặt</th>
                <th>Trạng thái</th>
                <th>Hạng thành viên</th>
                <th>Tạm tính</th>
                <th>Giảm giá</th>
                <th>Tổng thanh toán</th>
                <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <% if (orders.isEmpty()) { %>
                <tr><td colspan="9">Chưa có đơn hàng.</td></tr>
            <% } else { %>
                <% for (OrderStore.Order order : orders) { %>
                    <%
                        String orderTier = order.getMemberTierSnapshot() == null ? "" : order.getMemberTierSnapshot().trim();
                        if (orderTier.isEmpty() || "STANDARD".equalsIgnoreCase(orderTier)) {
                            orderTier = "Vô hạng";
                        }
                    %>
                    <tr>
                        <td><%= order.getId() %></td>
                        <td><%= order.getCustomerName() %></td>
                        <td><%= order.getCreatedAt() %></td>
                        <td><span class="status-pill"><%= order.getStatus().getLabel() %></span></td>
                        <td><%= orderTier %></td>
                        <td><%= order.getSubtotal().toPlainString() %> VND</td>
                        <td><%= order.getDiscountAmount().toPlainString() %> VND</td>
                        <td><%= order.getTotal().toPlainString() %> VND</td>
                        <td>
                            <% if (order.getStatus() == OrderStore.OrderStatus.CHO_XAC_NHAN) { %>
                                <form action="<%= request.getContextPath() %>/branch/dashboard" method="post" style="margin:0;">
                                    <input type="hidden" name="orderId" value="<%= order.getId() %>">
                                    <input type="hidden" name="action" value="confirm">
                                    <button class="action-btn" type="submit">Xác nhận đơn</button>
                                </form>
                            <% } else if (order.getStatus() == OrderStore.OrderStatus.DA_XAC_NHAN) { %>
                                <form action="<%= request.getContextPath() %>/branch/dashboard" method="post" style="margin:0;">
                                    <input type="hidden" name="orderId" value="<%= order.getId() %>">
                                    <input type="hidden" name="action" value="ship">
                                    <button class="action-btn" type="submit">Đã vận chuyển</button>
                                </form>
                            <% } else if (order.getStatus() == OrderStore.OrderStatus.DANG_GIAO) { %>
                                <form action="<%= request.getContextPath() %>/branch/dashboard" method="post" style="margin:0;">
                                    <input type="hidden" name="orderId" value="<%= order.getId() %>">
                                    <input type="hidden" name="action" value="deliver">
                                    <button class="action-btn" type="submit">Đã giao thành công</button>
                                </form>
                            <% } else { %>
                                -
                            <% } %>
                        </td>
                    </tr>
                <% } %>
            <% } %>
            </tbody>
        </table>
    <% } %>
</div>
</body>
</html>
