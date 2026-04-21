<%@page import="com.btl_web.model.User"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.btl_web.model.ClothingStore" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null || !currentUser.isCompanyOwner()) {
        session.setAttribute("authError", "Chỉ tài khoản admin mới được truy cập trang quản lý sản phẩm.");
        response.sendRedirect(request.getContextPath() + "/auth/login");
        return;
    }

    String formError = (String) session.getAttribute("formError");
    String successMessage = (String) session.getAttribute("successMessage");
    session.removeAttribute("formError");
    session.removeAttribute("successMessage");

    List<ClothingStore.ClothingItem> items = ClothingStore.all();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Linen Lab | Quản lý sản phẩm</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            min-height: 100vh;
            font-family: "Plus Jakarta Sans", "Segoe UI", sans-serif;
            color: #22302f;
            background: linear-gradient(180deg, #faf8f2 0%, #f1ece2 100%);
            padding: 12px 16px 24px;
        }

        .wrap {
            max-width: 1280px;
            margin: 0 auto;
        }

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

        .logo {
            font-size: 1.05rem;
            font-weight: 700;
            letter-spacing: -0.02em;
        }

        .links {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
            align-items: center;
        }

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
            cursor: pointer;
        }

        .btn.primary {
            background: #0d6c63;
            color: #fff;
            border-color: #0d6c63;
        }

        .btn.logout {
            background: #6b1f24;
            color: #fff;
            border-color: #6b1f24;
        }

        .container {
            display: grid;
            grid-template-columns: 240px 1fr;
            gap: 14px;
            margin-bottom: 14px;
        }

        .sidebar {
            background: #fffdf7;
            border: 1px solid #dbe4de;
            border-radius: 16px;
            padding: 14px;
            height: fit-content;
            position: sticky;
            top: 100px;
        }

        .sidebar h3 {
            margin: 0 0 12px;
            font-size: 0.95rem;
            color: #22302f;
        }

        .sidebar .actions {
            display: flex;
            flex-direction: column;
            gap: 8px;
        }

        .content {
            background: #fffdf7;
            border: 1px solid #dbe4de;
            border-radius: 16px;
            box-shadow: 0 16px 36px rgba(36, 47, 45, 0.1);
            padding: 16px;
        }

        .message {
            border-radius: 10px;
            padding: 10px 12px;
            border: 1px solid;
            font-size: 0.92rem;
            margin-bottom: 12px;
            animation: slideIn 0.3s ease-out;
        }

        .message.error {
            background: #fff1ef;
            border-color: #f2c8c2;
            color: #9e2a1d;
        }

        .message.success {
            background: #effaf7;
            border-color: #b7e0d9;
            color: #0c615a;
        }

        .stats {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 10px;
            margin-bottom: 14px;
        }

        .stat-card {
            background: #f9fcfb;
            border: 1px solid #e7eee9;
            border-radius: 12px;
            padding: 12px;
            text-align: center;
        }

        .stat-label {
            font-size: 0.75rem;
            color: #607270;
            text-transform: uppercase;
            letter-spacing: 0.06em;
            margin-bottom: 6px;
        }

        .stat-value {
            font-size: 1.3rem;
            font-weight: 700;
            color: #0d6c63;
        }

        .table-wrap {
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th, td {
            text-align: left;
            padding: 10px;
            border-bottom: 1px solid #e7eee9;
            font-size: 0.92rem;
        }

        th {
            background: #f3f7f5;
            font-size: 0.78rem;
            text-transform: uppercase;
            letter-spacing: 0.06em;
            color: #4d6563;
            font-weight: 700;
        }

        tbody tr:hover {
            background: #fbfdfc;
        }

        .empty {
            text-align: center;
            color: #667976;
            padding: 24px;
        }

        .btn-row {
            display: flex;
            gap: 6px;
        }

        .btn-edit, .btn-delete {
            padding: 6px 10px;
            border: none;
            border-radius: 8px;
            font-size: 0.78rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s;
        }

        .btn-edit {
            background: #e8f3f0;
            color: #0d6c63;
        }

        .btn-edit:hover {
            background: #d0e8e4;
        }

        .btn-delete {
            background: #fde8e4;
            color: #b42318;
        }

        .btn-delete:hover {
            background: #f5ccc3;
        }

        .modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.4);
            animation: fadeIn 0.3s;
        }

        .modal.show {
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .modal-content {
            background: #fffdf7;
            padding: 20px;
            border-radius: 16px;
            box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
            width: 90%;
            max-width: 450px;
            animation: slideUp 0.3s;
            position: relative;
        }

        .modal-header {
            margin-bottom: 16px;
        }

        .modal-header h2 {
            margin: 0;
            font-size: 1.1rem;
            font-weight: 700;
            color: #22302f;
        }

        .modal-close {
            position: absolute;
            right: 12px;
            top: 12px;
            background: none;
            border: none;
            font-size: 24px;
            cursor: pointer;
            color: #667976;
        }

        .form-group {
            margin-bottom: 12px;
        }

        .form-group label {
            display: block;
            font-weight: 600;
            margin-bottom: 5px;
            font-size: 0.88rem;
            color: #45615f;
        }

        .form-group input,
        .form-group select {
            width: 100%;
            padding: 9px 10px;
            border: 1px solid #d2dbd5;
            border-radius: 10px;
            font: inherit;
            color: #22302f;
            background: #fff;
        }

        .form-group input:focus,
        .form-group select:focus {
            outline: none;
            border-color: #0d6c63;
            box-shadow: 0 0 0 3px rgba(13, 108, 99, 0.1);
        }

        .form-actions {
            display: flex;
            gap: 8px;
            margin-top: 16px;
        }

        .btn-submit {
            flex: 1;
            padding: 9px;
            background: #0d6c63;
            color: #fff;
            border: none;
            border-radius: 10px;
            font-weight: 600;
            cursor: pointer;
        }

        .btn-submit:hover {
            filter: brightness(0.95);
        }

        .btn-cancel {
            flex: 1;
            padding: 9px;
            background: #e8ede9;
            color: #22302f;
            border: none;
            border-radius: 10px;
            font-weight: 600;
            cursor: pointer;
        }

        .btn-cancel:hover {
            background: #dce3df;
        }

        @keyframes fadeIn {
            from {
                opacity: 0;
            }
            to {
                opacity: 1;
            }
        }

        @keyframes slideUp {
            from {
                transform: translateY(20px);
                opacity: 0;
            }
            to {
                transform: translateY(0);
                opacity: 1;
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

        @media (max-width: 900px) {
            .container {
                grid-template-columns: 1fr;
            }

            .sidebar {
                position: static;
            }

            .stats {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 600px) {
            .topbar {
                flex-direction: column;
                align-items: flex-start;
            }

            .links {
                width: 100%;
                justify-content: flex-start;
            }

            table {
                font-size: 0.85rem;
            }

            th, td {
                padding: 8px;
            }

            .btn-edit, .btn-delete {
                padding: 4px 8px;
                font-size: 0.7rem;
            }

            .modal-content {
                width: 95%;
                padding: 16px;
            }
        }
    </style>
</head>
<body>
<div class="wrap">
    <div class="topbar">
        <div class="logo">Linen Lab</div>
        <div class="links">
            <span class="badge">Xin chào, <%= currentUser.getFullName() %></span>
            <a class="btn" href="<%= request.getContextPath() %>/shop">Về shop</a>
            <a class="btn" href="<%= request.getContextPath() %>/orders">Đơn hàng</a>
            <a class="btn" href="<%= request.getContextPath() %>/profile">Trang cá nhân</a>
            <a class="btn logout" href="<%= request.getContextPath() %>/auth/logout">Đăng xuất</a>
        </div>
    </div>

    <div class="container">
        <div class="sidebar">
            <h3>Thao tác</h3>
            <div class="actions">
                <button class="btn primary" onclick="openAddModal()">+ Thêm sản phẩm</button>
            </div>
        </div>

        <div class="content">
        <% if (formError != null) { %>
        <div class="message error"><%= formError %></div>
        <% } %>

        <% if (successMessage != null) { %>
        <div class="message success"><%= successMessage %></div>
        <% } %>

        <div class="stats">
            <div class="stat-card">
                <div class="stat-label">Tổng sản phẩm</div>
                <div class="stat-value"><%= items.size() %></div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Tồn kho</div>
                <div class="stat-value"><%= items.stream().mapToInt(item -> item.getStockQuantity()).sum() %></div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Trạng thái</div>
                <div class="stat-value"><%= items.isEmpty() ? "Trống" : "Hoạt động" %></div>
            </div>
        </div>

        <div class="card">
            <div class="table-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>Mã SP</th>
                        <th>Tên sản phẩm</th>
                        <th>Loại</th>
                        <th>Size</th>
                        <th>Màu</th>
                        <th>Giá</th>
                        <th>Tồn</th>
                        <th>Hành động</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% if (items.isEmpty()) { %>
                    <tr>
                        <td class="empty" colspan="8">Chưa có sản phẩm. <button class="btn-add" onclick="openAddModal()" style="background: none; color: var(--accent); border: none; text-decoration: underline; cursor: pointer; font-weight: 600;">Thêm sản phẩm đầu tiên</button></td>
                    </tr>
                    <% } else { %>
                    <% for (ClothingStore.ClothingItem item : items) { %>
                    <tr>
                        <td><strong><%= item.getProductCode() %></strong></td>
                        <td><%= item.getName() %></td>
                        <td><%= item.getCategory() %></td>
                        <td><%= item.getSize() %></td>
                        <td><%= item.getColor() %></td>
                        <td><%= String.format("%,.0f", item.getPrice().doubleValue()) %></td>
                        <td><span style="background: <%= item.getStockQuantity() > 0 ? "#e8f3f0" : "#ffe8e4" %>; padding: 4px 8px; border-radius: 6px;"><%= item.getStockQuantity() %></span></td>
                        <td>
                            <div class="btn-row">
                                <button class="btn-edit" onclick="editProduct('<%= item.getProductCode() %>')">Sửa</button>
                                <button class="btn-delete" onclick="deleteProduct('<%= item.getProductCode() %>', '<%= item.getName() %>')">Xoá</button>
                            </div>
                        </td>
                    </tr>
                    <% } %>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- Modal thêm/sửa sản phẩm -->
    <div id="productModal" class="modal">
        <div class="modal-content">
            <button class="modal-close" onclick="closeModal()">×</button>
            <div class="modal-header">
                <h2 id="modalTitle">Thêm sản phẩm mới</h2>
            </div>
            <form id="productForm" method="POST" action="">
                <div class="form-group">
                    <label for="productCode">Mã sản phẩm *</label>
                    <input type="text" id="productCode" name="productCode" required>
                </div>
                <div class="form-group">
                    <label for="name">Tên sản phẩm *</label>
                    <input type="text" id="name" name="name" required>
                </div>
                <div class="form-group">
                    <label for="category">Loại quần áo *</label>
                    <select id="category" name="category" required>
                        <option value="">-- Chọn loại --</option>
                        <option value="Áo thun">Áo thun</option>
                        <option value="Áo sơ mi">Áo sơ mi</option>
                        <option value="Quần jean">Quần jean</option>
                        <option value="Váy">Váy</option>
                        <option value="Áo khoác">Áo khoác</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="size">Size *</label>
                    <input type="text" id="size" name="size" placeholder="S, M, L, XL" required>
                </div>
                <div class="form-group">
                    <label for="color">Màu sắc *</label>
                    <input type="text" id="color" name="color" required>
                </div>
                <div class="form-group">
                    <label for="price">Giá bán (VND) *</label>
                    <input type="number" id="price" name="price" step="1000" required>
                </div>
                <div class="form-group">
                    <label for="stockQuantity">Số lượng tồn *</label>
                    <input type="number" id="stockQuantity" name="stockQuantity" min="0" required>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn-submit" id="submitBtn">Thêm</button>
                    <button type="button" class="btn-cancel" onclick="closeModal()">Hủy</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        let isEditMode = false;
        let originalProductCode = '';

        function openAddModal() {
            isEditMode = false;
            document.getElementById('modalTitle').textContent = 'Thêm sản phẩm mới';
            document.getElementById('submitBtn').textContent = 'Thêm';
            document.getElementById('productCode').readOnly = false;
            document.getElementById('productCode').style.backgroundColor = '#fff';
            document.getElementById('productForm').action = '<%= request.getContextPath() %>/clothes/preview';
            document.getElementById('productCode').value = '';
            document.getElementById('name').value = '';
            document.getElementById('category').value = '';
            document.getElementById('size').value = '';
            document.getElementById('color').value = '';
            document.getElementById('price').value = '';
            document.getElementById('stockQuantity').value = '';
            document.getElementById('productModal').classList.add('show');
        }

        function editProduct(productCode) {
            fetch('<%= request.getContextPath() %>/api/product?code=' + encodeURIComponent(productCode))
                .then(r => r.json())
                .then(data => {
                    if (data.error) {
                        alert('Lỗi: ' + data.error);
                        return;
                    }
                    isEditMode = true;
                    originalProductCode = productCode;
                    document.getElementById('modalTitle').textContent = 'Sửa sản phẩm';
                    document.getElementById('submitBtn').textContent = 'Cập nhật';
                    document.getElementById('productCode').readOnly = true;
                    document.getElementById('productCode').style.backgroundColor = '#f5f7f6';
                    document.getElementById('productForm').action = '<%= request.getContextPath() %>/clothes/update';
                    document.getElementById('productCode').value = data.productCode;
                    document.getElementById('name').value = data.name;
                    document.getElementById('category').value = data.category;
                    document.getElementById('size').value = data.size;
                    document.getElementById('color').value = data.color;
                    document.getElementById('price').value = data.price;
                    document.getElementById('stockQuantity').value = data.stockQuantity;
                    document.getElementById('productModal').classList.add('show');
                })
                .catch(e => alert('Lỗi: ' + e.message));
        }

        function deleteProduct(productCode, productName) {
            if (confirm('Bạn chắc chắn muốn xoá "' + productName + '"?')) {
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = '<%= request.getContextPath() %>/clothes/delete';
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'productCode';
                input.value = productCode;
                form.appendChild(input);
                document.body.appendChild(form);
                form.submit();
            }
        }

        function closeModal() {
            document.getElementById('productModal').classList.remove('show');
        }

        document.getElementById('productModal').addEventListener('click', function(event) {
            if (event.target === this) {
                closeModal();
            }
        });

        document.getElementById('productForm').addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(this);
            fetch(this.action, {
                method: 'POST',
                body: formData
            }).then(r => {
                if (r.ok) {
                    location.reload();
                } else {
                    alert('Lỗi: ' + r.statusText);
                }
            }).catch(e => alert('Lỗi: ' + e.message));
        });
    </script>
</body>
</html>
