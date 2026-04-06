package com.btl_web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = { "/cart", "/cart/add", "/cart/remove", "/cart/checkout" })
public class CartServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getSession().getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        HttpSession session = request.getSession();
        UserStore.User currentUser = (UserStore.User) session.getAttribute("currentUser");
        UserStore.User latestUser = UserStore.findByUsername(getServletContext(), currentUser.getUsername());
        List<CartItemView> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<String, Integer> entry : getCart(session).entrySet()) {
            ShopCatalog.Product product = ShopCatalog.findById(getServletContext(), entry.getKey());
            if (product == null) {
                continue;
            }
            int quantity = entry.getValue();
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            total = total.add(lineTotal);
            items.add(new CartItemView(product, quantity, lineTotal));
        }

        request.setAttribute("cartItems", items);
        request.setAttribute("cartTotal", total);
        request.setAttribute("profileReady", UserStore.isCheckoutProfileReady(latestUser));
        request.setAttribute("defaultAddressId", latestUser == null ? "" : latestUser.getDefaultAddressId());
        request.setAttribute("profileUser", latestUser);
        request.getRequestDispatcher("/cart.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String path = request.getServletPath();
        if (request.getSession().getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        if ("/cart/add".equals(path)) {
            addToCart(request, response);
            return;
        }

        if ("/cart/remove".equals(path)) {
            removeFromCart(request, response);
            return;
        }

        if ("/cart/checkout".equals(path)) {
            checkout(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/cart");
    }

    private void addToCart(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String productId = normalize(request.getParameter("productId"));
        int quantity = parsePositiveInt(request.getParameter("quantity"));

        if (quantity <= 0 || ShopCatalog.findById(getServletContext(), productId) == null) {
            request.getSession().setAttribute("shopError", "Không thể thêm sản phẩm vào giỏ.");
            response.sendRedirect(request.getContextPath() + "/shop");
            return;
        }

        Map<String, Integer> cart = getCart(request.getSession());
        cart.put(productId, cart.getOrDefault(productId, 0) + quantity);

        request.getSession().setAttribute("shopSuccess", "Đã thêm sản phẩm vào giỏ hàng.");
        response.sendRedirect(request.getContextPath() + "/shop");
    }

    private void removeFromCart(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String productId = normalize(request.getParameter("productId"));
        getCart(request.getSession()).remove(productId);
        response.sendRedirect(request.getContextPath() + "/cart");
    }

    private void checkout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        UserStore.User currentUser = (UserStore.User) request.getSession().getAttribute("currentUser");
        UserStore.User latestUser = UserStore.findByUsername(getServletContext(), currentUser.getUsername());
        if (!UserStore.isCheckoutProfileReady(latestUser)) {
            request.getSession().setAttribute(
                    "profileError",
                    "Trước khi đặt hàng, bạn cần cập nhật thông tin cá nhân cố định và thiết lập địa chỉ giao hàng mặc định.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        Map<String, Integer> cart = getCart(request.getSession());
        if (cart.isEmpty()) {
            request.getSession().setAttribute("shopError", "Giỏ hàng đang trống.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        String[] selectedProductIds = request.getParameterValues("selectedProductId");
        if (selectedProductIds == null || selectedProductIds.length == 0) {
            request.getSession().setAttribute("shopError", "Vui lòng chọn ít nhất một sản phẩm để đặt hàng.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        Map<String, Integer> selectedItems = new HashMap<>();
        for (String selectedProductId : selectedProductIds) {
            String normalizedProductId = normalize(selectedProductId);
            Integer quantity = cart.get(normalizedProductId);
            if (quantity != null) {
                selectedItems.put(normalizedProductId, quantity);
            }
        }

        if (selectedItems.isEmpty()) {
            request.getSession().setAttribute("shopError", "Không có sản phẩm hợp lệ nào được chọn.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        List<OrderStore.OrderLine> lines = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : selectedItems.entrySet()) {
            ShopCatalog.Product product = ShopCatalog.findById(getServletContext(), entry.getKey());
            if (product != null) {
                lines.add(new OrderStore.OrderLine(product.getId(), product.getName(), entry.getValue(),
                        product.getPrice()));
            }
        }

        if (lines.isEmpty()) {
            request.getSession().setAttribute("shopError", "Không thể tạo đơn hàng từ giỏ hàng hiện tại.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        OrderStore.createOrder(getServletContext(), latestUser, lines, totalForLines(lines));

        for (String selectedProductId : selectedItems.keySet()) {
            cart.remove(selectedProductId);
        }
        est.getSession().setAttribute("shopSuccess", "Đặt hàng thành công. Cảm ơn bạn đã mua sắm!"
        onse.sendRedirect(request.getContextPath() + "/orders");
        
    

    BigDecimal total = BigDecimal.ZERO;for(
    OrderStore.OrderLine line : line){
    
        total = total.add(line.getLineTotal());
        
            otal;
                    
        
    

    at Map<String, Inte
    e
        ct value = session.getAttribute("cart");
        value == null) {
        Map<Str
     

    }
        return (Map<String, Integer>) value;
    }

    private int parsePositiveInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return 1;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public static final class CartItemView {
        private final ShopCatalog.Product product;
        private final int quantity;
        private final BigDecimal lineTotal;

        public CartItemView(ShopCatalog.Product product, int quantity, BigDecimal lineTotal) {
            this.product = product;
            this.quantity = quantity;
            this.lineTotal = lineTotal;
        }

        public ShopCatalog.Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getLineTotal() {
            return lineTotal;
        }
    }

}

    
        
    

    
        
    

    
        
    
