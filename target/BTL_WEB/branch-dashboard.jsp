<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.btl_web.dao.BusinessReportDAO" %>
<%@ page import="com.btl_web.model.OrderStore" %>
<%
    BusinessReportDAO.BranchStat stat = (BusinessReportDAO.BranchStat) request.getAttribute("branchStat");
    String branchError = (String) request.getAttribute("branchError");
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
    <style>
        body { font-family: "Plus Jakarta Sans", sans-serif; background: #f6f3eb; margin: 0; padding: 16px; color: #22302f; }
        .wrap { max-width: 1200px; margin: 0 auto; }
        .top { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; }
        .btn { text-decoration: none; padding: 8px 12px; border-radius: 10px; border: 1px solid #cfd8d3; background: #fff; color: #234; font-weight: 600; }
        .grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; margin-bottom: 14px; }
        .card { background: #fffdf7; border: 1px solid #dbe4de; border-radius: 14px; padding: 14px; }
        .k { font-size: 0.82rem; color: #607270; text-transform: uppercase; letter-spacing: 0.06em; }
        .v { font-size: 1.2rem; font-weight: 700; }
        table { width: 100%; border-collapse: collapse; background: #fff; border: 1px solid #dbe4de; border-radius: 14px; overflow: hidden; }
        th, td { padding: 10px; border-bottom: 1px solid #e7eee9; text-align: left; }
        th { background: #f3f7f5; font-size: 0.82rem; }
        .err { padding: 12px; border-radius: 10px; background: #fff1ef; border: 1px solid #f2c8c2; color: #9e2a1d; }
        @media (max-width: 900px) { .grid { grid-template-columns: 1fr; } }
    </style>
</head>
<body>
<div class="wrap">
    <div class="top">
        <h1>Dashboard Chủ Chi Nhánh</h1>
        <div>
            <a class="btn" href="<%= request.getContextPath() %>/shop">Về shop</a>
            <a class="btn" href="<%= request.getContextPath() %>/auth/logout">Đăng xuất</a>
        </div>
    </div>

    <% if (branchError != null) { %>
        <div class="err"><%= branchError %></div>
    <% } else if (stat != null) { %>
        <div class="grid">
            <div class="card"><div class="k">Chi nhánh</div><div class="v"><%= stat.getBranchName() %></div></div>
            <div class="card"><div class="k">Đơn hàng</div><div class="v"><%= stat.getOrderCount() %></div></div>
            <div class="card"><div class="k">Doanh số</div><div class="v"><%= stat.getRevenue().toPlainString() %> VND</div></div>
            <div class="card"><div class="k">Tồn kho</div><div class="v"><%= stat.getTotalInventory() %></div></div>
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
                <th>Hạng thành viên</th>
                <th>Tạm tính</th>
                <th>Giảm giá</th>
                <th>Tổng thanh toán</th>
            </tr>
            </thead>
            <tbody>
            <% if (orders.isEmpty()) { %>
                <tr><td colspan="7">Chưa có đơn hàng.</td></tr>
            <% } else { %>
                <% for (OrderStore.Order order : orders) { %>
                    <tr>
                        <td><%= order.getId() %></td>
                        <td><%= order.getCustomerName() %></td>
                        <td><%= order.getCreatedAt() %></td>
                        <td><%= order.getMemberTierSnapshot() %></td>
                        <td><%= order.getSubtotal().toPlainString() %> VND</td>
                        <td><%= order.getDiscountAmount().toPlainString() %> VND</td>
                        <td><%= order.getTotal().toPlainString() %> VND</td>
                    </tr>
                <% } %>
            <% } %>
            </tbody>
        </table>
    <% } %>
</div>
</body>
</html>
