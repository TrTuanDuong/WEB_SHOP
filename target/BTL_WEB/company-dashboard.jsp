<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.btl_web.dao.BusinessReportDAO" %>
<%@ page import="com.btl_web.model.User" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/auth/login");
        return;
    }

    BusinessReportDAO.CompanySummary summary = (BusinessReportDAO.CompanySummary) request.getAttribute("companySummary");
    @SuppressWarnings("unchecked")
    List<BusinessReportDAO.BranchStat> branchStats = (List<BusinessReportDAO.BranchStat>) request.getAttribute("branchStats");
    String dashboardError = (String) request.getAttribute("dashboardError");
    String dashboardSuccess = (String) request.getAttribute("dashboardSuccess");
    String draftBranchId = (String) request.getAttribute("draftBranchId");
    String draftBranchName = (String) request.getAttribute("draftBranchName");
    String draftBranchAddress = (String) request.getAttribute("draftBranchAddress");
    String draftOwnerUsername = (String) request.getAttribute("draftOwnerUsername");
    String draftOwnerFullName = (String) request.getAttribute("draftOwnerFullName");

    if (draftBranchId == null) draftBranchId = "";
    if (draftBranchName == null) draftBranchName = "";
    if (draftBranchAddress == null) draftBranchAddress = "";
    if (draftOwnerUsername == null) draftOwnerUsername = "";
    if (draftOwnerFullName == null) draftOwnerFullName = "";

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
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --line: #d5e0da;
            --bg: #f5f2ea;
            --panel: #fffdf7;
            --text: #20312f;
            --muted: #607270;
            --accent: #0f6f66;
        }
        * { box-sizing: border-box; }
        body {
            font-family: "Plus Jakarta Sans", sans-serif;
            margin: 0;
            background: linear-gradient(180deg, #faf8f2 0%, #f1ece2 100%);
            color: var(--text);
            padding: 12px 16px 24px;
        }
        .wrap { max-width: 1260px; margin: 0 auto; }
        .topbar {
            position: sticky;
            top: 10px;
            z-index: 20;
            border: 1px solid var(--line);
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
            border: 1px solid #ced8d2;
            border-radius: 10px;
            padding: 8px 12px;
            color: #2b4442;
            background: #fff;
            font-weight: 600;
            font-size: 0.88rem;
        }
        .btn.primary {
            background: linear-gradient(180deg, #0f766e 0%, #0d6c63 100%);
            color: #fff;
            border-color: #0d6c63;
        }
        .msg {
            margin-bottom: 12px;
            padding: 10px 12px;
            border-radius: 10px;
            font-size: 0.9rem;
        }
        .err { background: #fff1ef; border: 1px solid #f2c8c2; color: #9e2a1d; }
        .ok { background: #ecfaf6; border: 1px solid #b7e1d8; color: #0f6159; }
        .grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-bottom: 12px; }
        .card { background: var(--panel); border: 1px solid var(--line); border-radius: 14px; padding: 14px; }
        .k { font-size: 0.82rem; color: var(--muted); text-transform: uppercase; letter-spacing: 0.06em; }
        .v { font-size: 1.4rem; font-weight: 700; }
        .shell { display: grid; grid-template-columns: 420px 1fr; gap: 12px; }
        .field { margin-bottom: 10px; }
        label { display: block; margin-bottom: 5px; font-size: 0.84rem; font-weight: 700; color: #34504e; }
        input {
            width: 100%;
            border: 1px solid #d2dbd5;
            border-radius: 10px;
            padding: 10px;
            font: inherit;
            color: var(--text);
            background: #fff;
        }
        .submit-btn {
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
        table {
            width: 100%;
            border-collapse: collapse;
            background: #fff;
            border: 1px solid var(--line);
            border-radius: 14px;
            overflow: hidden;
        }
        th, td { padding: 10px; border-bottom: 1px solid #e7eee9; text-align: left; }
        th { background: #f3f7f5; font-size: 0.82rem; }
        .desc { margin: 0 0 10px; color: #5f7270; }
        @media (max-width: 1080px) {
            .shell { grid-template-columns: 1fr; }
            .grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<div class="wrap">
    <div class="topbar">
        <div class="logo">Linen Lab | Dashboard công ty</div>
        <div class="links">
            <span class="badge">Xin chào, <%= currentUser.getFullName() %></span>
            <a class="btn" href="<%= request.getContextPath() %>/addproducts">Thêm sản phẩm</a>
            <a class="btn" href="<%= request.getContextPath() %>/shop">Về shop</a>
            <a class="btn" href="<%= request.getContextPath() %>/profile">Trang cá nhân</a>
            <a class="btn" href="<%= request.getContextPath() %>/auth/logout">Đăng xuất</a>
        </div>
    </div>

    <% if (dashboardError != null) { %><div class="msg err"><%= dashboardError %></div><% } %>
    <% if (dashboardSuccess != null) { %><div class="msg ok"><%= dashboardSuccess %></div><% } %>

    <div class="grid">
        <div class="card"><div class="k">Tổng doanh số công ty</div><div class="v"><%= summary.getTotalRevenue().toPlainString() %> VND</div></div>
        <div class="card"><div class="k">Tổng hàng tồn kho</div><div class="v"><%= summary.getTotalInventory() %></div></div>
        <div class="card"><div class="k">Tổng số sản phẩm của cả hãng</div><div class="v"><%= summary.getTotalProducts() %></div></div>
    </div>

    <div class="shell">
        <section class="card">
            <h2>Tạo chi nhánh mới + tài khoản đăng nhập</h2>
            <p class="desc">Admin tạo một lần, hệ thống sẽ sinh quyền Chủ chi nhánh cho tài khoản này.</p>
            <form action="<%= request.getContextPath() %>/company/dashboard" method="post">
                <div class="field">
                    <label for="branchId">Mã chi nhánh</label>
                    <input id="branchId" name="branchId" value="<%= draftBranchId %>" placeholder="Ví dụ: CN_DN">
                </div>
                <div class="field">
                    <label for="branchName">Tên chi nhánh</label>
                    <input id="branchName" name="branchName" value="<%= draftBranchName %>" placeholder="Ví dụ: Chi nhánh Đà Nẵng">
                </div>
                <div class="field">
                    <label for="branchAddress">Địa chỉ chi nhánh</label>
                    <input id="branchAddress" name="branchAddress" value="<%= draftBranchAddress %>" placeholder="Ví dụ: Quận Hải Châu, Đà Nẵng">
                </div>
                <div class="field">
                    <label for="ownerUsername">Tài khoản đăng nhập chi nhánh</label>
                    <input id="ownerUsername" name="ownerUsername" value="<%= draftOwnerUsername %>" placeholder="Ví dụ: branch_dn">
                </div>
                <div class="field">
                    <label for="ownerFullName">Tên chủ chi nhánh</label>
                    <input id="ownerFullName" name="ownerFullName" value="<%= draftOwnerFullName %>" placeholder="Ví dụ: Chủ chi nhánh Đà Nẵng">
                </div>
                <div class="field">
                    <label for="ownerPassword">Mật khẩu chủ chi nhánh</label>
                    <input id="ownerPassword" type="password" name="ownerPassword" placeholder="Tối thiểu 6 ký tự">
                </div>
                <button class="submit-btn" type="submit">Tạo chi nhánh và tài khoản</button>
            </form>
        </section>

        <section>
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
        </section>
    </div>
</div>
</body>
</html>
