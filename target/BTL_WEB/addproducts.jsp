<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.btl_web.model.ClothingStore" %>
<%@ page import="com.btl_web.model.UserStore" %>
<%
    UserStore.User currentUser = (UserStore.User) session.getAttribute("currentUser");
    if (currentUser == null || !currentUser.isAdmin()) {
        session.setAttribute("authError", "Chỉ tài khoản admin mới được truy cập trang quản trị sản phẩm.");
        response.sendRedirect(request.getContextPath() + "/auth/login");
        return;
    }

    String enteredProductCode = (String) request.getAttribute("enteredProductCode");
    String enteredName = (String) request.getAttribute("enteredName");
    String enteredCategory = (String) request.getAttribute("enteredCategory");
    String enteredSize = (String) request.getAttribute("enteredSize");
    String enteredColor = (String) request.getAttribute("enteredColor");
    String enteredPrice = (String) request.getAttribute("enteredPrice");
    String enteredStockQuantity = (String) request.getAttribute("enteredStockQuantity");

    if (enteredProductCode == null && session.getAttribute("enteredProductCode") != null) {
        enteredProductCode = (String) session.getAttribute("enteredProductCode");
        session.removeAttribute("enteredProductCode");
    }
    if (enteredName == null && session.getAttribute("enteredName") != null) {
        enteredName = (String) session.getAttribute("enteredName");
        session.removeAttribute("enteredName");
    }
    if (enteredCategory == null && session.getAttribute("enteredCategory") != null) {
        enteredCategory = (String) session.getAttribute("enteredCategory");
        session.removeAttribute("enteredCategory");
    }
    if (enteredSize == null && session.getAttribute("enteredSize") != null) {
        enteredSize = (String) session.getAttribute("enteredSize");
        session.removeAttribute("enteredSize");
    }
    if (enteredColor == null && session.getAttribute("enteredColor") != null) {
        enteredColor = (String) session.getAttribute("enteredColor");
        session.removeAttribute("enteredColor");
    }
    if (enteredPrice == null && session.getAttribute("enteredPrice") != null) {
        enteredPrice = (String) session.getAttribute("enteredPrice");
        session.removeAttribute("enteredPrice");
    }
    if (enteredStockQuantity == null && session.getAttribute("enteredStockQuantity") != null) {
        enteredStockQuantity = (String) session.getAttribute("enteredStockQuantity");
        session.removeAttribute("enteredStockQuantity");
    }

    if (enteredProductCode == null) enteredProductCode = "";
    if (enteredName == null) enteredName = "";
    if (enteredSize == null) enteredSize = "";
    if (enteredColor == null) enteredColor = "";
    if (enteredPrice == null) enteredPrice = "";
    if (enteredStockQuantity == null) enteredStockQuantity = "";

    String lastCategory = (String) session.getAttribute("lastCategory");
    String selectedCategory = enteredCategory != null && !enteredCategory.isEmpty()
            ? enteredCategory
            : (lastCategory != null ? lastCategory : "Áo thun");

    String errorProductCode = (String) request.getAttribute("errorProductCode");
    String errorName = (String) request.getAttribute("errorName");
    String errorCategory = (String) request.getAttribute("errorCategory");
    String errorSize = (String) request.getAttribute("errorSize");
    String errorColor = (String) request.getAttribute("errorColor");
    String errorPrice = (String) request.getAttribute("errorPrice");
    String errorStockQuantity = (String) request.getAttribute("errorStockQuantity");

    if (errorProductCode == null && session.getAttribute("errorProductCode") != null) {
        errorProductCode = (String) session.getAttribute("errorProductCode");
        session.removeAttribute("errorProductCode");
    }

    String formError = (String) session.getAttribute("formError");
    String successMessage = (String) session.getAttribute("successMessage");
    session.removeAttribute("formError");
    session.removeAttribute("successMessage");

    List<ClothingStore.ClothingItem> items = ClothingStore.all(application);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Linen Lab | Thêm sản phẩm</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Space+Grotesk:wght@500;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg: #f8f7f3;
            --bg-soft: #f0eee8;
            --panel: #fffdf8;
            --text: #1b2b2a;
            --muted: #60706f;
            --line: #dde3de;
            --accent: #0f766e;
            --accent-soft: #e4f4f1;
            --danger: #b42318;
            --radius-lg: 22px;
            --radius-md: 14px;
            --shadow: 0 18px 45px rgba(27, 43, 42, 0.08);
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            min-height: 100vh;
            font-family: "Plus Jakarta Sans", "Segoe UI", sans-serif;
            color: var(--text);
            background:
                radial-gradient(circle at 12% 10%, #fbe9d5 0 14%, transparent 40%),
                radial-gradient(circle at 88% 6%, #daf0ec 0 18%, transparent 45%),
                linear-gradient(180deg, #fbfaf7 0%, #f5f2eb 100%);
            padding: 40px 18px 56px;
        }

        .container {
            max-width: 1080px;
            margin: 0 auto;
            display: grid;
            gap: 22px;
            animation: pageIn 0.6s ease-out;
        }

        .hero {
            padding: 24px 26px;
            border: 1px solid var(--line);
            border-radius: var(--radius-lg);
            background: rgba(255, 253, 248, 0.88);
            backdrop-filter: blur(4px);
            box-shadow: var(--shadow);
        }

        .eyebrow {
            display: inline-block;
            padding: 6px 12px;
            border-radius: 999px;
            background: var(--accent-soft);
            color: var(--accent);
            font-size: 12px;
            font-weight: 700;
            letter-spacing: 0.08em;
            text-transform: uppercase;
        }

        .hero h1 {
            margin: 12px 0 8px;
            font-family: "Space Grotesk", sans-serif;
            font-size: clamp(1.6rem, 1.3rem + 1.2vw, 2.4rem);
            letter-spacing: -0.02em;
        }

        .hero p {
            margin: 0;
            color: var(--muted);
            max-width: 680px;
        }

        .hero-link {
            display: inline-block;
            margin-top: 12px;
            padding: 9px 12px;
            border-radius: 10px;
            text-decoration: none;
            background: #153f3b;
            color: #fff;
            font-weight: 600;
            font-size: 0.9rem;
        }

        .message {
            border-radius: var(--radius-md);
            border: 1px solid;
            padding: 12px 14px;
            animation: slideIn 0.3s ease-out;
        }

        .message.error {
            border-color: #f2c6c2;
            background: #fff4f3;
            color: var(--danger);
        }

        .message.success {
            border-color: #b7e0d9;
            background: #effaf7;
            color: #0c615a;
        }

        .layout {
            display: grid;
            grid-template-columns: 1.05fr 1fr;
            gap: 22px;
        }

        .card {
            border-radius: var(--radius-lg);
            border: 1px solid var(--line);
            background: var(--panel);
            box-shadow: var(--shadow);
            overflow: hidden;
        }

        .card-header {
            padding: 18px 22px 14px;
            border-bottom: 1px solid var(--line);
            background: linear-gradient(180deg, #fffefb 0%, #faf8f2 100%);
        }

        .card-title {
            margin: 0;
            font-size: 1.05rem;
            font-weight: 700;
            font-family: "Space Grotesk", sans-serif;
            letter-spacing: -0.01em;
        }

        .card-sub {
            margin: 4px 0 0;
            color: var(--muted);
            font-size: 0.9rem;
        }

        form {
            padding: 18px 22px 22px;
            display: grid;
            gap: 12px;
        }

        .field {
            display: grid;
            gap: 6px;
        }

        label {
            font-size: 0.86rem;
            font-weight: 600;
            color: #34504e;
        }

        input,
        select {
            width: 100%;
            border: 1px solid #d8dfda;
            background: #fff;
            border-radius: 12px;
            padding: 10px 12px;
            font: inherit;
            color: var(--text);
            transition: border-color 0.2s, box-shadow 0.2s;
        }

        input:focus,
        select:focus {
            outline: none;
            border-color: #5da89f;
            box-shadow: 0 0 0 3px rgba(93, 168, 159, 0.2);
        }

        .error-text {
            font-size: 0.82rem;
            color: var(--danger);
        }

        .actions {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            margin-top: 6px;
        }

        button {
            border: 0;
            border-radius: 12px;
            padding: 10px 14px;
            font: inherit;
            font-weight: 600;
            cursor: pointer;
        }

        .btn-primary {
            background: var(--accent);
            color: #fff;
        }

        .btn-secondary {
            background: #edf2ef;
            color: #385451;
        }

        .btn-primary:hover {
            filter: brightness(0.95);
        }

        .btn-secondary:hover {
            background: #e4ebe7;
        }

        .stats {
            padding: 18px 22px;
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 10px;
        }

        .chip {
            background: #f8faf9;
            border: 1px solid var(--line);
            border-radius: 12px;
            padding: 12px;
        }

        .chip .k {
            font-size: 0.77rem;
            color: var(--muted);
            text-transform: uppercase;
            letter-spacing: 0.07em;
            margin-bottom: 6px;
        }

        .chip .v {
            font-family: "Space Grotesk", sans-serif;
            font-size: 1.08rem;
        }

        .table-wrap {
            padding: 14px;
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: separate;
            border-spacing: 0;
            min-width: 720px;
        }

        th,
        td {
            text-align: left;
            padding: 12px 10px;
            border-bottom: 1px solid #e5ebe7;
            font-size: 0.9rem;
            white-space: nowrap;
        }

        th {
            position: sticky;
            top: 0;
            z-index: 1;
            background: #f7faf8;
            color: #456361;
            font-size: 0.78rem;
            text-transform: uppercase;
            letter-spacing: 0.08em;
        }

        tr:hover td {
            background: #fbfdfc;
        }

        .empty {
            text-align: center;
            color: var(--muted);
            padding: 22px 12px;
        }

        @media (max-width: 920px) {
            .layout {
                grid-template-columns: 1fr;
            }

            .stats {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 560px) {
            body {
                padding: 24px 12px 38px;
            }

            .hero,
            .card-header,
            form,
            .stats,
            .table-wrap {
                padding-left: 14px;
                padding-right: 14px;
            }

            .actions {
                flex-direction: column;
            }

            button {
                width: 100%;
            }
        }

        @keyframes pageIn {
            from {
                opacity: 0;
                transform: translateY(16px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        @keyframes slideIn {
            from {
                opacity: 0;
                transform: translateY(-8px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
    </style>
</head>
<body>
<div class="container">
    <section class="hero">
        <span class="eyebrow">Trang quản trị</span>
        <h1>Thêm sản phẩm quần áo</h1>
        <p>Nhập nhanh sản phẩm mới, kiểm tra hợp lệ tức thì và theo dõi danh sách đã thêm ngay trong cùng một màn hình.</p>
        <a class="hero-link" href="<%= request.getContextPath() %>/shop">Mở trang mua hàng cho khách</a>
    </section>

    <% if (formError != null) { %>
    <div class="message error"><%= formError %></div>
    <% } %>

    <% if (successMessage != null) { %>
    <div class="message success"><%= successMessage %></div>
    <% } %>

    <section class="layout">
        <article class="card">
            <div class="card-header">
                <h2 class="card-title">Thông tin sản phẩm mới</h2>
                <p class="card-sub">Kiểm tra dữ liệu trước khi qua bước xác nhận.</p>
            </div>

            <form action="<%= request.getContextPath() %>/clothes/preview" method="post">
                <div class="field">
                    <label for="productCode">Mã sản phẩm</label>
                    <input id="productCode" type="text" name="productCode" value="<%= enteredProductCode %>" placeholder="VD: AO-THUN-001">
                    <% if (errorProductCode != null) { %><span class="error-text"><%= errorProductCode %></span><% } %>
                </div>

                <div class="field">
                    <label for="name">Tên sản phẩm</label>
                    <input id="name" type="text" name="name" value="<%= enteredName %>" placeholder="Áo thun cổ tròn basic">
                    <% if (errorName != null) { %><span class="error-text"><%= errorName %></span><% } %>
                </div>

                <div class="field">
                    <label for="category">Loại quần áo</label>
                    <select id="category" name="category">
                        <option value="Áo thun" <%= "Áo thun".equals(selectedCategory) ? "selected" : "" %>>Áo thun</option>
                        <option value="Áo sơ mi" <%= "Áo sơ mi".equals(selectedCategory) ? "selected" : "" %>>Áo sơ mi</option>
                        <option value="Quần jean" <%= "Quần jean".equals(selectedCategory) ? "selected" : "" %>>Quần jean</option>
                        <option value="Váy" <%= "Váy".equals(selectedCategory) ? "selected" : "" %>>Váy</option>
                        <option value="Áo khoác" <%= "Áo khoác".equals(selectedCategory) ? "selected" : "" %>>Áo khoác</option>
                    </select>
                    <% if (errorCategory != null) { %><span class="error-text"><%= errorCategory %></span><% } %>
                </div>

                <div class="field">
                    <label for="size">Size</label>
                    <input id="size" type="text" name="size" value="<%= enteredSize %>" placeholder="S / M / L / XL">
                    <% if (errorSize != null) { %><span class="error-text"><%= errorSize %></span><% } %>
                </div>

                <div class="field">
                    <label for="color">Màu sắc</label>
                    <input id="color" type="text" name="color" value="<%= enteredColor %>" placeholder="Đen, Trắng, Xanh navy...">
                    <% if (errorColor != null) { %><span class="error-text"><%= errorColor %></span><% } %>
                </div>

                <div class="field">
                    <label for="price">Giá bán (VND)</label>
                    <input id="price" type="text" name="price" value="<%= enteredPrice %>" placeholder="299000">
                    <% if (errorPrice != null) { %><span class="error-text"><%= errorPrice %></span><% } %>
                </div>

                <div class="field">
                    <label for="stockQuantity">Số lượng tồn</label>
                    <input id="stockQuantity" type="text" name="stockQuantity" value="<%= enteredStockQuantity %>" placeholder="120">
                    <% if (errorStockQuantity != null) { %><span class="error-text"><%= errorStockQuantity %></span><% } %>
                </div>

                <div class="actions">
                    <button class="btn-primary" type="submit">Xem trước</button>
                    <button class="btn-secondary" type="button" onclick="clearClothingForm()">Làm mới form</button>
                </div>
            </form>
        </article>

        <article class="card">
            <div class="card-header">
                <h2 class="card-title">Tổng quan nhanh</h2>
                <p class="card-sub">Theo dõi dữ liệu bạn đã nhập ngay trong phiên làm việc.</p>
            </div>

            <div class="stats">
                <div class="chip">
                    <div class="k">Sản phẩm đã thêm</div>
                    <div class="v"><%= items.size() %></div>
                </div>
                <div class="chip">
                    <div class="k">Loại gần nhất</div>
                    <div class="v"><%= selectedCategory %></div>
                </div>
                <div class="chip">
                    <div class="k">Trạng thái</div>
                    <div class="v"><%= items.isEmpty() ? "Chưa có dữ liệu" : "Đang hoạt động" %></div>
                </div>
            </div>

            <div class="table-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>Mã SP</th>
                        <th>Tên sản phẩm</th>
                        <th>Loại</th>
                        <th>Size</th>
                        <th>Màu</th>
                        <th>Giá (VND)</th>
                        <th>Tồn kho</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% if (items.isEmpty()) { %>
                    <tr>
                        <td class="empty" colspan="7">Chưa có sản phẩm nào. Hãy thêm sản phẩm đầu tiên.</td>
                    </tr>
                    <% } else { %>
                    <% for (ClothingStore.ClothingItem item : items) { %>
                    <tr>
                        <td><%= item.getProductCode() %></td>
                        <td><%= item.getName() %></td>
                        <td><%= item.getCategory() %></td>
                        <td><%= item.getSize() %></td>
                        <td><%= item.getColor() %></td>
                        <td><%= item.getPrice().toPlainString() %></td>
                        <td><%= item.getStockQuantity() %></td>
                    </tr>
                    <% } %>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </article>
    </section>
</div>

<script>
    function clearClothingForm() {
        var form = document.querySelector('form[action$="/clothes/preview"]');
        if (!form) return;

        form.querySelectorAll('input[type="text"]').forEach(function (input) {
            input.value = '';
        });

        form.querySelectorAll('.error-text').forEach(function (node) {
            node.remove();
        });

        document.querySelectorAll('.message').forEach(function (node) {
            node.remove();
        });
    }
</script>
</body>
</html>
