<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.btl_web.ShopCatalog" %>
<%@ page import="com.btl_web.UserStore" %>
<%
    @SuppressWarnings("unchecked")
    List<ShopCatalog.Product> products = (List<ShopCatalog.Product>) request.getAttribute("products");
    if (products == null) {
        products = java.util.Collections.emptyList();
    }

    String group = (String) request.getAttribute("group");
    String segment = (String) request.getAttribute("segment");
    String q = (String) request.getAttribute("q");
    Integer pageNumberObj = (Integer) request.getAttribute("page");
    Integer totalPagesObj = (Integer) request.getAttribute("totalPages");
    Integer totalItemsObj = (Integer) request.getAttribute("totalItems");

    int pageNumber = pageNumberObj == null ? 1 : pageNumberObj;
    int totalPages = totalPagesObj == null ? 1 : totalPagesObj;
    int totalItems = totalItemsObj == null ? 0 : totalItemsObj;

    if (group == null) group = "all";
    if (segment == null) segment = "all";
    if (q == null) q = "";

    UserStore.User currentUser = (UserStore.User) session.getAttribute("currentUser");
    String authSuccess = (String) session.getAttribute("authSuccess");
    String shopSuccess = (String) session.getAttribute("shopSuccess");
    String shopError = (String) session.getAttribute("shopError");
    session.removeAttribute("authSuccess");
    session.removeAttribute("shopSuccess");
    session.removeAttribute("shopError");

    @SuppressWarnings("unchecked")
    java.util.Map<String, Integer> cart = (java.util.Map<String, Integer>) session.getAttribute("cart");
    int cartCount = 0;
    if (cart != null) {
        for (Integer value : cart.values()) {
            cartCount += value;
        }
    }

    String querySuffix = "&group=" + java.net.URLEncoder.encode(group, java.nio.charset.StandardCharsets.UTF_8)
            + "&segment=" + java.net.URLEncoder.encode(segment, java.nio.charset.StandardCharsets.UTF_8)
            + "&q=" + java.net.URLEncoder.encode(q, java.nio.charset.StandardCharsets.UTF_8);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Shop Quần Áo | Mua hàng</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Archivo+Black&display=swap" rel="stylesheet">
    <style>
        :root {
            --panel: #fffdf7;
            --text: #222f2e;
            --muted: #6b7b79;
            --line: #dde4de;
            --accent: #0d6c63;
            --accent-soft: #e6f4f1;
            --danger: #b42318;
            --radius-lg: 18px;
            --radius-md: 12px;
            --shadow: 0 10px 28px rgba(35, 48, 46, 0.08);
        }

        * { box-sizing: border-box; }

        body {
            margin: 0;
            font-family: "Plus Jakarta Sans", "Segoe UI", sans-serif;
            color: var(--text);
            background:
                radial-gradient(circle at 16% 8%, #fae6ce 0 15%, transparent 38%),
                radial-gradient(circle at 88% 14%, #d9efea 0 16%, transparent 42%),
                linear-gradient(180deg, #faf8f2 0%, #f4f1ea 100%);
            min-height: 100vh;
            padding: 12px 20px 24px;
        }

        .topbar {
            max-width: 1500px;
            margin: 0 auto 14px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 12px;
            flex-wrap: wrap;
            position: sticky;
            top: 10px;
            z-index: 20;
            border: 1px solid #d6e0da;
            background: rgba(255, 253, 247, 0.92);
            backdrop-filter: blur(6px);
            border-radius: 14px;
            box-shadow: var(--shadow);
            padding: 10px 12px;
        }

        .logo {
            font-family: "Archivo Black", sans-serif;
            letter-spacing: 0.03em;
            font-size: 1.25rem;
        }

        .top-actions {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
            align-items: center;
        }

        .badge {
            background: var(--accent-soft);
            color: var(--accent);
            border-radius: 999px;
            padding: 6px 10px;
            font-size: 0.82rem;
            font-weight: 700;
        }

        .link-btn {
            display: inline-block;
            text-decoration: none;
            border: 1px solid #ced8d2;
            border-radius: 10px;
            padding: 8px 12px;
            color: #2b4442;
            background: #fff;
            font-weight: 600;
            font-size: 0.9rem;
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

        .layout {
            max-width: 1500px;
            margin: 0 auto;
            display: grid;
            grid-template-columns: 290px 1fr;
            gap: 16px;
        }

        .sidebar,
        .content {
            background: var(--panel);
            border: 1px solid var(--line);
            border-radius: var(--radius-lg);
            box-shadow: var(--shadow);
        }

        .sidebar {
            padding: 18px;
            position: sticky;
            top: 90px;
            height: fit-content;
        }

        .sidebar h2,
        .content h2 {
            margin: 0 0 12px;
            font-size: 1rem;
        }

        .field {
            margin-bottom: 12px;
        }

        .field label {
            display: block;
            font-size: 0.82rem;
            font-weight: 700;
            margin-bottom: 5px;
            color: #45615f;
            text-transform: uppercase;
            letter-spacing: 0.06em;
        }

        .field input,
        .field select {
            width: 100%;
            border: 1px solid #d2dbd5;
            border-radius: 10px;
            padding: 9px 10px;
            font: inherit;
            background: #fff;
        }

        .sidebar .actions {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
        }

        .btn {
            border: 0;
            border-radius: 10px;
            padding: 9px 12px;
            font: inherit;
            font-weight: 600;
            cursor: pointer;
        }

        .btn-primary { background: var(--accent); color: #fff; }

        .content {
            padding: 16px;
        }

        .messages { display: grid; gap: 8px; margin-bottom: 12px; }

        .message {
            border-radius: 10px;
            padding: 10px 12px;
            border: 1px solid;
            font-size: 0.92rem;
        }

        .message.ok { background: #effaf7; border-color: #b9e0d8; color: #0f6159; }
        .message.err { background: #fff4f3; border-color: #f0c8c3; color: var(--danger); }

        .summary {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 10px;
            margin-bottom: 12px;
            flex-wrap: wrap;
        }

        .summary .count {
            color: var(--muted);
            font-size: 0.92rem;
        }

        .grid {
            display: grid;
            grid-template-columns: repeat(5, minmax(0, 1fr));
            gap: 12px;
        }

        .card {
            border: 1px solid #d9e2dc;
            border-radius: 14px;
            background: #fff;
            padding: 10px;
            display: grid;
            gap: 8px;
        }

        .thumb {
            height: 120px;
            border-radius: 10px;
            background:
                linear-gradient(135deg, #e9f6f2 0%, #f7f3e8 100%),
                repeating-linear-gradient(45deg, rgba(13,108,99,0.06) 0 9px, rgba(13,108,99,0.02) 9px 18px);
            border: 1px solid #dbe6e1;
        }

        .card h3 {
            margin: 0;
            font-size: 0.92rem;
            line-height: 1.3;
            min-height: 2.3em;
        }

        .meta {
            font-size: 0.78rem;
            color: var(--muted);
            display: flex;
            flex-wrap: wrap;
            gap: 6px;
        }

        .price {
            font-weight: 700;
            color: var(--accent);
        }

        .buy {
            display: grid;
            gap: 6px;
        }

        .buy-row {
            display: flex;
            gap: 6px;
        }

        .buy-row input {
            width: 62px;
            border: 1px solid #d2dbd5;
            border-radius: 8px;
            padding: 6px;
            text-align: center;
        }

        .buy-row button {
            flex: 1;
            border: 0;
            border-radius: 8px;
            background: #153f3b;
            color: #fff;
            font-weight: 600;
            cursor: pointer;
        }

        .login-note {
            font-size: 0.78rem;
            color: var(--muted);
        }

        .pagination {
            margin-top: 14px;
            display: flex;
            gap: 6px;
            flex-wrap: wrap;
        }

        .pagination a,
        .pagination span {
            border: 1px solid #d0dad4;
            border-radius: 8px;
            padding: 6px 10px;
            font-size: 0.86rem;
            text-decoration: none;
            color: #334e4b;
            background: #fff;
        }

        .pagination .active {
            border-color: var(--accent);
            background: var(--accent);
            color: #fff;
        }

        .empty {
            padding: 30px 12px;
            text-align: center;
            color: var(--muted);
            border: 1px dashed #ccd8d2;
            border-radius: 12px;
            background: #fcfdfb;
        }

        @media (max-width: 1320px) {
            .grid { grid-template-columns: repeat(4, minmax(0, 1fr)); }
        }

        @media (max-width: 1080px) {
            .layout { grid-template-columns: 1fr; }
            .sidebar { position: static; }
            .grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
        }

        @media (max-width: 760px) {
            body { padding: 10px; }
            .grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
            .topbar { top: 6px; }
        }

        @media (max-width: 520px) {
            .grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<div class="topbar">
    <div class="logo">Linen Lab Shop</div>
    <div class="top-actions">
        <% if (currentUser == null) { %>
            <a class="link-btn" href="<%= request.getContextPath() %>/auth/login">Đăng nhập</a>
            <a class="link-btn primary" href="<%= request.getContextPath() %>/auth/register">Đăng ký</a>
        <% } else { %>
            <span class="badge">Xin chào, <%= currentUser.getFullName() %></span>
            <% if ("admin".equals(currentUser.getUsername())) { %>
                <a class="link-btn primary" href="<%= request.getContextPath() %>/addproducts">Thêm sản phẩm</a>
            <% } %>
            <a class="link-btn" href="<%= request.getContextPath() %>/profile">Trang cá nhân</a>
            <a class="link-btn" href="<%= request.getContextPath() %>/cart">Giỏ hàng(<%= cartCount %>)</a>
            <a class="link-btn" href="<%= request.getContextPath() %>/orders">Đơn hàng</a>
            <% if ("admin".equals(currentUser.getUsername())) { %>
                <a class="link-btn" href="<%= request.getContextPath() %>/admin-contact">Yêu cầu</a>
            <% } else { %>
                <a class="link-btn" href="<%= request.getContextPath() %>/admin-contact">Liên hệ admin</a>
            <% } %>
            <a class="link-btn logout" href="<%= request.getContextPath() %>/auth/logout">Đăng xuất</a>
        <% } %>
    </div>
</div>

<div class="layout">
    <aside class="sidebar">
        <h2>Bộ lọc sản phẩm</h2>
        <form id="filterForm" action="<%= request.getContextPath() %>/shop" method="get">
            <div class="field">
                <label for="q">Tìm kiếm</label>
                <input id="q" name="q" value="<%= q %>" placeholder="Mã hoặc tên sản phẩm">
            </div>

            <div class="field">
                <label for="group">Phân loại chính</label>
                <select id="group" name="group">
                    <option value="all" <%= "all".equals(group) ? "selected" : "" %>>Tất cả</option>
                    <option value="Người lớn" <%= "Người lớn".equals(group) ? "selected" : "" %>>Người lớn</option>
                    <option value="Trẻ em" <%= "Trẻ em".equals(group) ? "selected" : "" %>>Trẻ em</option>
                </select>
            </div>

            <div class="field">
                <label for="segment">Nhóm chi tiết</label>
                <select id="segment" name="segment"></select>
            </div>

            <div class="actions">
                <button class="btn btn-primary" type="submit">Áp dụng</button>
                <a class="link-btn" href="<%= request.getContextPath() %>/shop">Xóa lọc</a>
            </div>
        </form>
    </aside>

    <section class="content">
        <div class="messages">
            <% if (authSuccess != null) { %><div class="message ok"><%= authSuccess %></div><% } %>
            <% if (shopSuccess != null) { %><div class="message ok"><%= shopSuccess %></div><% } %>
            <% if (shopError != null) { %><div class="message err"><%= shopError %></div><% } %>
        </div>

        <div class="summary">
            <h2>Danh mục quần áo</h2>
            <div class="count">Tổng <strong><%= totalItems %></strong> sản phẩm | 1 trang hiển thị tối đa 40 sản phẩm (5 cột x 8 hàng)</div>
        </div>

        <% if (products.isEmpty()) { %>
            <div class="empty">Không có sản phẩm phù hợp bộ lọc hiện tại.</div>
        <% } else { %>
            <div class="grid">
                <% for (ShopCatalog.Product product : products) { %>
                    <article class="card">
                        <div class="thumb"></div>
                        <h3><%= product.getName() %></h3>
                        <div class="meta">
                            <span><%= product.getGroup() %></span>
                            <span>|</span>
                            <span><%= product.getSegment() %></span>
                            <span>|</span>
                            <span>Size <%= product.getSize() %></span>
                            <span>|</span>
                            <span><%= product.getColor() %></span>
                        </div>
                        <div class="price"><%= product.getPrice().toPlainString() %> VND</div>
                        <div class="buy">
                            <% if (currentUser == null) { %>
                                <div class="login-note">Đăng nhập để thêm vào giỏ hàng.</div>
                                <a class="link-btn" href="<%= request.getContextPath() %>/auth/login">Đăng nhập để mua</a>
                            <% } else { %>
                                <form action="<%= request.getContextPath() %>/cart/add" method="post">
                                    <input type="hidden" name="productId" value="<%= product.getId() %>">
                                    <div class="buy-row">
                                        <input type="number" name="quantity" min="1" value="1">
                                        <button type="submit">Thêm vào giỏ</button>
                                    </div>
                                </form>
                            <% } %>
                        </div>
                    </article>
                <% } %>
            </div>
        <% } %>

        <div class="pagination">
            <% if (pageNumber > 1) { %>
                <a href="<%= request.getContextPath() %>/shop?page=<%= pageNumber - 1 %><%= querySuffix %>">Trang trước</a>
            <% } %>

            <% for (int i = 1; i <= totalPages; i++) { %>
                <% if (i == pageNumber) { %>
                    <span class="active"><%= i %></span>
                <% } else { %>
                    <a href="<%= request.getContextPath() %>/shop?page=<%= i %><%= querySuffix %>"><%= i %></a>
                <% } %>
            <% } %>

            <% if (pageNumber < totalPages) { %>
                <a href="<%= request.getContextPath() %>/shop?page=<%= pageNumber + 1 %><%= querySuffix %>">Trang sau</a>
            <% } %>
        </div>
    </section>
</div>

<script>
    (function () {
        var groupSelect = document.getElementById('group');
        var segmentSelect = document.getElementById('segment');
        var selectedSegment = '<%= segment %>';

        function optionsForGroup(group) {
            if (group === 'Người lớn') {
                return [
                    { value: 'all', label: 'Tất cả (Người lớn)' },
                    { value: 'male', label: 'Nam' },
                    { value: 'female', label: 'Nữ' }
                ];
            }
            if (group === 'Trẻ em') {
                return [
                    { value: 'all', label: 'Tất cả (Trẻ em)' },
                    { value: 'boy', label: 'Bé trai' },
                    { value: 'girl', label: 'Bé gái' }
                ];
            }
            return [
                { value: 'all', label: 'Tất cả' },
                { value: 'male', label: 'Người lớn - Nam' },
                { value: 'female', label: 'Người lớn - Nữ' },
                { value: 'boy', label: 'Trẻ em - Bé trai' },
                { value: 'girl', label: 'Trẻ em - Bé gái' }
            ];
        }

        function renderSegmentOptions() {
            var group = groupSelect.value;
            var options = optionsForGroup(group);
            var allowedValues = options.map(function (item) { return item.value; });
            var current = allowedValues.indexOf(selectedSegment) >= 0 ? selectedSegment : 'all';

            segmentSelect.innerHTML = '';
            options.forEach(function (item) {
                var option = document.createElement('option');
                option.value = item.value;
                option.textContent = item.label;
                if (item.value === current) {
                    option.selected = true;
                }
                segmentSelect.appendChild(option);
            });
        }

        groupSelect.addEventListener('change', function () {
            selectedSegment = 'all';
            renderSegmentOptions();
        });

        segmentSelect.addEventListener('change', function () {
            selectedSegment = segmentSelect.value;
        });

        renderSegmentOptions();
    })();
</script>
</body>
</html>
