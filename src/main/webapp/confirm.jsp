<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="javax.servlet.http.HttpSession" %>
<%
    HttpSession currentSession = request.getSession();
    String productCode = (String) currentSession.getAttribute("draftProductCode");
    String name = (String) currentSession.getAttribute("draftName");
    String category = (String) currentSession.getAttribute("draftCategory");
    String size = (String) currentSession.getAttribute("draftSize");
    String color = (String) currentSession.getAttribute("draftColor");
    String price = (String) currentSession.getAttribute("draftPrice");
    String stockQuantity = (String) currentSession.getAttribute("draftStockQuantity");

    if (productCode == null || name == null || category == null || size == null
            || color == null || price == null || stockQuantity == null) {
        response.sendRedirect(request.getContextPath() + "/addproducts");
        return;
    }
    
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Linen Lab | Xác nhận sản phẩm</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Space+Grotesk:wght@500;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg: #f8f7f3;
            --panel: #fffdf8;
            --line: #dce3de;
            --text: #1b2b2a;
            --muted: #60706f;
            --accent: #0f766e;
            --accent-soft: #e4f4f1;
            --radius-lg: 22px;
            --radius-md: 12px;
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
                radial-gradient(circle at 18% 8%, #fbe9d5 0 14%, transparent 38%),
                radial-gradient(circle at 84% 14%, #daf0ec 0 15%, transparent 42%),
                linear-gradient(180deg, #fbfaf7 0%, #f5f2eb 100%);
            padding: 40px 18px;
        }

        .container {
            max-width: 760px;
            margin: 0 auto;
            display: grid;
            gap: 18px;
            animation: showIn 0.55s ease-out;
        }

        .hero {
            border: 1px solid var(--line);
            border-radius: var(--radius-lg);
            background: rgba(255, 253, 248, 0.9);
            box-shadow: var(--shadow);
            padding: 20px 22px;
        }

        .eyebrow {
            display: inline-block;
            padding: 6px 11px;
            border-radius: 999px;
            background: var(--accent-soft);
            color: var(--accent);
            font-size: 12px;
            font-weight: 700;
            letter-spacing: 0.08em;
            text-transform: uppercase;
        }

        h1 {
            margin: 12px 0 6px;
            font-family: "Space Grotesk", sans-serif;
            letter-spacing: -0.02em;
            font-size: clamp(1.3rem, 1.1rem + 1vw, 2rem);
        }

        .hero p {
            margin: 0;
            color: var(--muted);
        }

        .card {
            border: 1px solid var(--line);
            border-radius: var(--radius-lg);
            background: var(--panel);
            box-shadow: var(--shadow);
            overflow: hidden;
        }

        .rows {
            padding: 12px 16px;
        }

        .row {
            display: grid;
            grid-template-columns: 170px 1fr;
            gap: 8px;
            padding: 12px 8px;
            border-bottom: 1px solid #e9eeeb;
        }

        .row:last-child {
            border-bottom: 0;
        }

        .label {
            font-size: 0.82rem;
            text-transform: uppercase;
            letter-spacing: 0.08em;
            color: var(--muted);
            font-weight: 700;
        }

        .value {
            font-size: 0.98rem;
            font-weight: 600;
            color: #234241;
        }

        .actions {
            border-top: 1px solid var(--line);
            padding: 14px 16px 18px;
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }

        button {
            border: 0;
            border-radius: var(--radius-md);
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

        @media (max-width: 620px) {
            body {
                padding: 24px 12px;
            }

            .hero,
            .rows,
            .actions {
                padding-left: 12px;
                padding-right: 12px;
            }

            .row {
                grid-template-columns: 1fr;
                gap: 4px;
            }

            .actions {
                flex-direction: column;
            }

            button {
                width: 100%;
            }
        }

        @keyframes showIn {
            from {
                opacity: 0;
                transform: translateY(14px);
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
        <span class="eyebrow">Xác nhận</span>
        <h1>Kiểm tra thông tin trước khi lưu</h1>
        <p>Nếu dữ liệu chính xác, bấm xác nhận lưu. Nếu chưa đúng, quay lại để chỉnh sửa nhanh.</p>
    </section>

    <section class="card">
        <div class="rows">
            <div class="row"><div class="label">Mã sản phẩm</div><div class="value"><%= productCode %></div></div>
            <div class="row"><div class="label">Tên sản phẩm</div><div class="value"><%= name %></div></div>
            <div class="row"><div class="label">Loại quần áo</div><div class="value"><%= category %></div></div>
            <div class="row"><div class="label">Size</div><div class="value"><%= size %></div></div>
            <div class="row"><div class="label">Màu sắc</div><div class="value"><%= color %></div></div>
            <div class="row"><div class="label">Giá bán</div><div class="value"><%= price %> VND</div></div>
            <div class="row"><div class="label">Số lượng tồn</div><div class="value"><%= stockQuantity %></div></div>
        </div>

        <div class="actions">
            <form action="<%= request.getContextPath() %>/clothes/confirm" method="post">
                <button class="btn-primary" type="submit">Xác nhận lưu</button>
            </form>
            <form action="<%= request.getContextPath() %>/clothes/back" method="post">
                <button class="btn-secondary" type="submit">Quay lại chỉnh sửa</button>
            </form>
        </div>
    </section>
</div>
</body>
</html>
