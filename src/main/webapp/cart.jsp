<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="com.btl_web.controller.CartServlet" %>
<%@ page import="com.btl_web.model.UserStore" %>
<%
    @SuppressWarnings("unchecked")
    List<CartServlet.CartItemView> cartItems = (List<CartServlet.CartItemView>) request.getAttribute("cartItems");
    if (cartItems == null) {
        cartItems = java.util.Collections.emptyList();
    }
    BigDecimal cartTotal = (BigDecimal) request.getAttribute("cartTotal");
    if (cartTotal == null) cartTotal = BigDecimal.ZERO;

    UserStore.User currentUser = (UserStore.User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/auth/login");
        return;
    }

    UserStore.User profileUser = (UserStore.User) request.getAttribute("profileUser");
    if (profileUser == null) {
        profileUser = currentUser;
    }

    Boolean profileReadyObj = (Boolean) request.getAttribute("profileReady");
    boolean profileReady = profileReadyObj != null && profileReadyObj;

    String shopError = (String) session.getAttribute("shopError");
    session.removeAttribute("shopError");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giỏ hàng</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700&family=Archivo+Black&display=swap" rel="stylesheet">
    <style>
        body {
            margin: 0;
            min-height: 100vh;
            font-family: "Plus Jakarta Sans", sans-serif;
            background: linear-gradient(160deg, #f8f2e8 0%, #e7f2ef 100%);
            padding: 12px 18px 24px;
            color: #22302f;
        }

        .topbar {
            max-width: 1100px;
            margin: 0 auto 14px;
            position: sticky;
            top: 10px;
            z-index: 20;
            border: 1px solid #d6e0da;
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
        }

        .logo {
            font-family: "Archivo Black", sans-serif;
            font-size: 1.12rem;
        }

        .top-links {
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

        .link-btn.logout {
            border-color: #6b1f24;
            background: #6b1f24;
            color: #fff;
        }

        .container {
            max-width: 1100px;
            margin: 0 auto;
            border: 1px solid #dbe3de;
            border-radius: 16px;
            background: #fffdf9;
            box-shadow: 0 16px 36px rgba(36, 47, 45, 0.1);
            padding: 16px;
        }

        h1 { margin-top: 0; }

        .note {
            margin-bottom: 12px;
            padding: 10px;
            border-radius: 10px;
            font-size: 0.9rem;
            border: 1px solid;
        }

        .err { background: #fff3f2; color: #b42318; border-color: #f0c6c1; }
        .warn { background: #fff8e9; color: #8c5b10; border-color: #f0d8a8; }
        .ok { background: #ecfaf5; color: #0f6159; border-color: #b7e0d8; }

        table {
            width: 100%;
            border-collapse: collapse;
        }
        th, td {
            border-bottom: 1px solid #e4ebe6;
            text-align: left;
            padding: 10px 8px;
            font-size: 0.92rem;
        }
        th {
            background: #f6f9f8;
            font-size: 0.82rem;
            text-transform: uppercase;
            letter-spacing: 0.06em;
            color: #4d6563;
        }
        .empty {
            color: #6b7b79;
            text-align: center;
            padding: 20px;
        }
        .total {
            text-align: right;
            margin-top: 12px;
            font-size: 1rem;
            font-weight: 700;
        }
        .actions {
            margin-top: 14px;
            display: flex;
            gap: 8px;
            justify-content: flex-end;
            flex-wrap: wrap;
        }
        .btn {
            border: 0;
            border-radius: 10px;
            padding: 9px 12px;
            font: inherit;
            font-weight: 700;
            cursor: pointer;
        }
        .btn-primary { background: #0d6c63; color: #fff; }
        .btn-soft { background: #edf1ef; color: #395553; }
    </style>
</head>
<body>
<div class="topbar">
    <div class="logo">Linen Lab | Giỏ hàng</div>
    <div class="top-links">
        <a class="link-btn" href="<%= request.getContextPath() %>/orders">Đơn hàng</a>
        <a class="link-btn" href="<%= request.getContextPath() %>/shop">Tiếp tục mua sắm</a>
        <a class="link-btn" href="<%= request.getContextPath() %>/profile">Trang cá nhân</a>
        <a class="link-btn logout" href="<%= request.getContextPath() %>/auth/logout">Đăng xuất</a>
    </div>
</div>

<div class="container">
    <h1>Giỏ hàng của <%= profileUser.getFullName() %></h1>

    <% if (shopError != null) { %><div class="note err"><%= shopError %></div><% } %>

    <% if (!profileReady) { %>
        <div class="note warn">
            Trước khi đặt hàng, bạn bắt buộc phải hoàn thiện thông tin cá nhân cố định và thêm địa chỉ giao hàng mặc định.
            <a href="<%= request.getContextPath() %>/profile">Cập nhật ngay</a>
        </div>
    <% } else { %>
        <div class="note ok">Thông tin giao hàng đã sẵn sàng. Bạn có thể đặt hàng.</div>
    <% } %>

    <form id="cartCheckoutForm" action="<%= request.getContextPath() %>/cart/checkout" method="post">
        <div class="actions" style="justify-content:flex-start; margin-bottom:12px;">
            <button id="toggleSelectAllBtn" class="btn btn-soft" type="button">Chọn tất cả</button>
        </div>

        <table>
            <thead>
            <tr>
                <th>Chọn</th>
                <th>Mã SP</th>
                <th>Tên</th>
                <th>SL</th>
                <th>Đơn giá</th>
                <th>Thành tiền</th>
                <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <% if (cartItems.isEmpty()) { %>
                <tr><td colspan="7" class="empty">Giỏ hàng trống.</td></tr>
            <% } else { %>
                <% for (CartServlet.CartItemView item : cartItems) { %>
                    <tr data-line-total="<%= item.getLineTotal().toPlainString() %>">
                        <td>
                            <input class="cart-item-checkbox" type="checkbox" name="selectedProductId" value="<%= item.getProduct().getId() %>" checked>
                        </td>
                        <td><%= item.getProduct().getId() %></td>
                        <td><%= item.getProduct().getName() %></td>
                        <td><%= item.getQuantity() %></td>
                        <td><%= item.getProduct().getPrice().toPlainString() %> VND</td>
                        <td><%= item.getLineTotal().toPlainString() %> VND</td>
                        <td>
                            <input type="hidden" name="productId" value="<%= item.getProduct().getId() %>">
                            <button class="btn btn-soft" type="submit" formaction="<%= request.getContextPath() %>/cart/remove" formmethod="post">Xóa</button>
                        </td>
                    </tr>
                <% } %>
            <% } %>
            </tbody>
        </table>

        <div class="total">
            Tổng tiền: <span id="selectedTotal"><%= cartTotal.toPlainString() %></span> VND
        </div>
        <div class="note" style="margin-top: 8px; margin-bottom: 0; background: #f7fbfa; border-color: #dce9e4; color: #395553;">
            Đã chọn <strong id="selectedCount"><%= cartItems.size() %></strong> sản phẩm.
        </div>

        <div class="actions">
            <button class="btn btn-primary" type="submit" <%= !profileReady ? "disabled" : "" %>>Đặt hàng</button>
        </div>
    </form>
</div>

<script>
    (function () {
        var checkboxes = Array.prototype.slice.call(document.querySelectorAll('.cart-item-checkbox'));
        var selectAllButton = document.getElementById('toggleSelectAllBtn');
        var selectedTotalNode = document.getElementById('selectedTotal');
        var selectedCountNode = document.getElementById('selectedCount');

        function formatMoney(value) {
            return String(Math.round(value));
        }

        function updateSummary() {
            var selectedCheckboxes = checkboxes.filter(function (checkbox) { return checkbox.checked; });
            var total = 0;

            selectedCheckboxes.forEach(function (checkbox) {
                var row = checkbox.closest('tr');
                var lineTotal = row ? parseFloat(row.getAttribute('data-line-total') || '0') : 0;
                total += lineTotal;
            });

            selectedCountNode.textContent = String(selectedCheckboxes.length);
            selectedTotalNode.textContent = formatMoney(total);
            selectAllButton.textContent = selectedCheckboxes.length === checkboxes.length && checkboxes.length > 0
                ? 'Bỏ chọn tất cả'
                : 'Chọn tất cả';
        }

        selectAllButton.addEventListener('click', function () {
            var shouldSelectAll = selectAllButton.textContent !== 'Bỏ chọn tất cả';
            checkboxes.forEach(function (checkbox) {
                checkbox.checked = shouldSelectAll;
            });
            updateSummary();
        });

        checkboxes.forEach(function (checkbox) {
            checkbox.addEventListener('change', updateSummary);
        });

        updateSummary();
    })();
</script>
</body>
</html>
