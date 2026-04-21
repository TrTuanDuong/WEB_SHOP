<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.btl_web.dao.BusinessReportDAO" %>
<%
    BusinessReportDAO.CompanySummary summary = (BusinessReportDAO.CompanySummary) request.getAttribute("companySummary");
    @SuppressWarnings("unchecked")
    List<BusinessReportDAO.BranchStat> branchStats = (List<BusinessReportDAO.BranchStat>) request.getAttribute("branchStats");
    if (summary == null) {
        summary = new BusinessReportDAO.CompanySummary(java.math.BigDecimal.ZERO, 0, 0);
    }
    if (branchStats == null) {
        branchStats = java.util.Collections.emptyList();
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard Chủ Công Ty</title>
    <style>
        body { font-family: "Plus Jakarta Sans", sans-serif; background: #f6f3eb; margin: 0; padding: 16px; color: #22302f; }
        .wrap { max-width: 1200px; margin: 0 auto; }
        .top { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; }
        .btn { text-decoration: none; padding: 8px 12px; border-radius: 10px; border: 1px solid #cfd8d3; background: #fff; color: #234; font-weight: 600; }
        .grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-bottom: 14px; }
        .card { background: #fffdf7; border: 1px solid #dbe4de; border-radius: 14px; padding: 14px; }
        .k { font-size: 0.82rem; color: #607270; text-transform: uppercase; letter-spacing: 0.06em; }
        .v { font-size: 1.4rem; font-weight: 700; }
        table { width: 100%; border-collapse: collapse; background: #fff; border: 1px solid #dbe4de; border-radius: 14px; overflow: hidden; }
        th, td { padding: 10px; border-bottom: 1px solid #e7eee9; text-align: left; }
        th { background: #f3f7f5; font-size: 0.82rem; }
        @media (max-width: 900px) { .grid { grid-template-columns: 1fr; } }
    </style>
</head>
<body>
<div class="wrap">
    <div class="top">
        <h1>Dashboard Chủ Công Ty</h1>
        <div>
            <a class="btn" href="<%= request.getContextPath() %>/shop">Về shop</a>
            <a class="btn" href="<%= request.getContextPath() %>/auth/logout">Đăng xuất</a>
        </div>
    </div>

    <div class="grid">
        <div class="card"><div class="k">Tổng doanh số công ty</div><div class="v"><%= summary.getTotalRevenue().toPlainString() %> VND</div></div>
        <div class="card"><div class="k">Tổng tồn kho</div><div class="v"><%= summary.getTotalInventory() %></div></div>
        <div class="card"><div class="k">Tổng số sản phẩm</div><div class="v"><%= summary.getTotalProducts() %></div></div>
    </div>

    <table>
        <thead>
        <tr>
            <th>Chi nhánh</th>
            <th>Địa chỉ</th>
            <th>Chủ chi nhánh</th>
            <th>Số đơn</th>
            <th>Doanh số</th>
            <th>Tồn kho</th>
            <th>Số SKU</th>
        </tr>
        </thead>
        <tbody>
        <% for (BusinessReportDAO.BranchStat row : branchStats) { %>
            <tr>
                <td><%= row.getBranchName() %> (<%= row.getBranchId() %>)</td>
                <td><%= row.getBranchAddress() %></td>
                <td><%= row.getOwnerUsername() == null ? "" : row.getOwnerUsername() %></td>
                <td><%= row.getOrderCount() %></td>
                <td><%= row.getRevenue().toPlainString() %> VND</td>
                <td><%= row.getTotalInventory() %></td>
                <td><%= row.getProductCount() %></td>
            </tr>
        <% } %>
        </tbody>
    </table>
</div>
</body>
</html>
