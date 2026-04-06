<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String authError = (String) request.getAttribute("authError");
    String enteredFullName = (String) request.getAttribute("enteredFullName");
    String enteredUsername = (String) request.getAttribute("enteredUsername");
    if (enteredFullName == null) enteredFullName = "";
    if (enteredUsername == null) enteredUsername = "";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700&display=swap" rel="stylesheet">
    <style>
        body {
            margin: 0;
            min-height: 100vh;
            display: grid;
            place-items: center;
            background: linear-gradient(160deg, #f8f2e8 0%, #e7f2ef 100%);
            font-family: "Plus Jakarta Sans", sans-serif;
            padding: 16px;
        }
        .box {
            width: 100%;
            max-width: 460px;
            border: 1px solid #dbe3de;
            border-radius: 16px;
            background: #fffdf9;
            box-shadow: 0 16px 36px rgba(36, 47, 45, 0.1);
            padding: 18px;
        }
        h1 { margin: 0 0 6px; font-size: 1.3rem; }
        p { margin: 0 0 14px; color: #5f706f; }
        .field { margin-bottom: 10px; }
        label { display: block; margin-bottom: 5px; font-size: 0.86rem; font-weight: 700; }
        input {
            width: 100%;
            border: 1px solid #d2dbd5;
            border-radius: 10px;
            padding: 10px;
            font: inherit;
        }
        button {
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
        .msg { margin-bottom: 10px; padding: 9px; border-radius: 8px; font-size: 0.9rem; }
        .err { background: #fff3f2; color: #b42318; border: 1px solid #f0c6c1; }
        .links { margin-top: 12px; font-size: 0.9rem; display: flex; justify-content: space-between; gap: 8px; }
        a { color: #0d6c63; text-decoration: none; font-weight: 600; }
    </style>
</head>
<body>
<div class="box">
    <h1>Tạo tài khoản mua hàng</h1>
    <p>Đăng ký nhanh để thêm sản phẩm vào giỏ và đặt hàng.</p>

    <% if (authError != null) { %><div class="msg err"><%= authError %></div><% } %>

    <form action="<%= request.getContextPath() %>/auth/register" method="post">
        <div class="field">
            <label for="fullName">Họ và tên</label>
            <input id="fullName" name="fullName" value="<%= enteredFullName %>">
        </div>
        <div class="field">
            <label for="username">Tên đăng nhập</label>
            <input id="username" name="username" value="<%= enteredUsername %>">
        </div>
        <div class="field">
            <label for="password">Mật khẩu</label>
            <input id="password" type="password" name="password">
        </div>
        <div class="field">
            <label for="confirmPassword">Nhập lại mật khẩu</label>
            <input id="confirmPassword" type="password" name="confirmPassword">
        </div>
        <button type="submit">Đăng ký</button>
    </form>

    <div class="links">
        <a href="<%= request.getContextPath() %>/auth/login">Đã có tài khoản? Đăng nhập</a>
        <a href="<%= request.getContextPath() %>/shop">Quay về shop</a>
    </div>
</div>
</body>
</html>
