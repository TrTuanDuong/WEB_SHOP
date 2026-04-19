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
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --panel: #fffdf8;
            --text: #22302f;
            --muted: #60706f;
            --line: #dbe4de;
            --accent: #0d6c63;
            --accent-soft: #e7f4f1;
            --shadow: 0 18px 45px rgba(35, 48, 46, 0.08);
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            min-height: 100vh;
            display: grid;
            place-items: center;
            background:
                radial-gradient(circle at 14% 10%, #fae6ce 0 14%, transparent 34%),
                radial-gradient(circle at 88% 12%, #d9efea 0 14%, transparent 36%),
                linear-gradient(160deg, #f8f2e8 0%, #e7f2ef 100%);
            font-family: "Plus Jakarta Sans", sans-serif;
            color: var(--text);
            padding: 16px;
            line-height: 1.5;
            -webkit-font-smoothing: antialiased;
            text-rendering: optimizeLegibility;
        }

        .box {
            width: 100%;
            max-width: 520px;
            border: 1px solid var(--line);
            border-radius: 22px;
            background: rgba(255, 253, 248, 0.92);
            box-shadow: var(--shadow);
            padding: 24px;
            backdrop-filter: blur(4px);
        }

        .brand {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 14px;
            padding: 6px 10px;
            border-radius: 999px;
            background: var(--accent-soft);
            color: var(--accent);
            font-size: 0.8rem;
            font-weight: 700;
            letter-spacing: 0.08em;
            text-transform: uppercase;
        }

        h1 { margin: 0 0 6px; font-size: 1.3rem; }
        p { margin: 0 0 14px; color: var(--muted); }
        .field { margin-bottom: 12px; }
        label { display: block; margin-bottom: 6px; font-size: 0.84rem; font-weight: 700; color: #34504e; }
        input {
            width: 100%;
            border: 1px solid #d2dbd5;
            border-radius: 12px;
            padding: 12px 12px;
            font: inherit;
            color: var(--text);
            background: #fff;
            transition: border-color 0.2s, box-shadow 0.2s;
        }
        input:focus {
            outline: none;
            border-color: #5da89f;
            box-shadow: 0 0 0 3px rgba(93, 168, 159, 0.18);
        }
        button {
            width: 100%;
            border: 0;
            border-radius: 12px;
            padding: 12px;
            font: inherit;
            font-weight: 700;
            background: linear-gradient(180deg, #0f766e 0%, #0d6c63 100%);
            color: #fff;
            cursor: pointer;
            box-shadow: 0 10px 22px rgba(13, 108, 99, 0.22);
        }
        .msg {
            margin-bottom: 10px;
            padding: 10px 12px;
            border-radius: 10px;
            font-size: 0.9rem;
        }
        .err { background: #fff3f2; color: #b42318; border: 1px solid #f0c6c1; }
        .links {
            margin-top: 14px;
            display: flex;
            justify-content: space-between;
            gap: 10px;
            flex-wrap: wrap;
        }
        a { color: #0d6c63; text-decoration: none; font-weight: 700; }
        a:hover { text-decoration: underline; }
        @media (max-width: 520px) {
            .box { padding: 18px; }
            .links a { width: 100%; }
        }
    </style>
</head>
<body>
<div class="box">
    <div class="brand">Linen Lab</div>
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
