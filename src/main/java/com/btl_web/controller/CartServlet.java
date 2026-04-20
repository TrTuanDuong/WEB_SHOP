package com.btl_web.controller;

import com.btl_web.dao.ProductDAO;
import com.btl_web.dao.UserDAO;
import com.btl_web.model.CartStore;
import com.btl_web.model.OrderStore;
import com.btl_web.model.Product;
import com.btl_web.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/cart", "/cart/add", "/cart/remove", "/cart/checkout"})
public class CartServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();
    private ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        User latestUser = userDAO.findByUsername(getServletContext(), currentUser.getUsername());
        List<CartItemView> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<String, Integer> entry : getCart(currentUser.getUsername()).entrySet()) {
            try {
                Product product = productDAO.findById(getServletContext(), entry.getKey());
                int quantity = entry.getValue();
                BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
                total = total.add(lineTotal);
                items.add(new CartItemView(product, quantity, lineTotal));
            } catch (SQLException ex) {
                System.getLogger(CartServlet.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
          

        }

        request.setAttribute("cartItems", items);
        request.setAttribute("cartTotal", total);
        request.setAttribute("profileReady", userDAO.isCheckoutProfileReady(latestUser));
        request.setAttribute("defaultAddressId", latestUser == null ? "" : latestUser.getDefaultAddressId());
        request.setAttribute("profileUser", latestUser);
        request.getRequestDispatcher("/cart.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (request.getSession().getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String path = request.getServletPath();
        if ("/cart/add".equals(path)) {
            try {
                addToCart(request, response);
            } catch (SQLException ex) {
                System.getLogger(CartServlet.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            return;
        }
        if ("/cart/remove".equals(path)) {
            removeFromCart(request, response);
            return;
        }
        if ("/cart/checkout".equals(path)) {
            try {
                checkout(request, response);
            } catch (SQLException ex) {
                System.getLogger(CartServlet.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            return;
        }

        response.sendRedirect(request.getContextPath() + "/cart");
    }
    //Them vao gio hang cua khach hang
    private void addToCart(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        String productId = normalize(request.getParameter("productId"));
        int quantity = parsePositiveInt(request.getParameter("quantity"));

        if (quantity <= 0 || productDAO.findById(getServletContext(), productId) == null) {
            request.getSession().setAttribute("shopError", "Không thể thêm sản phẩm vào giỏ.");
            response.sendRedirect(request.getContextPath() + "/shop");
            return;
        }

        CartStore.addItem(getServletContext(), currentUser.getUsername(), productId, quantity);

        request.getSession().setAttribute("shopSuccess", "Đã thêm sản phẩm vào giỏ hàng.");
        response.sendRedirect(request.getContextPath() + "/shop");
    }

    private void removeFromCart(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        String productId = normalize(request.getParameter("productId"));
        CartStore.removeItem(getServletContext(), currentUser.getUsername(), productId);
        response.sendRedirect(request.getContextPath() + "/cart");
    }

    private void checkout(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        User latestUser = userDAO.findByUsername(getServletContext(), currentUser.getUsername());
        if (!userDAO.isCheckoutProfileReady(latestUser)) {
            request.getSession().setAttribute(
                    "profileError",
                    "Trước khi đặt hàng, bạn cần cập nhật thông tin cá nhân cố định và thiết lập địa chỉ giao hàng mặc định.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        Map<String, Integer> cart = getCart(currentUser.getUsername());
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
            Product product = productDAO.findById(getServletContext(), entry.getKey());
            if (product != null) {
                lines.add(new OrderStore.OrderLine(
                        product.getId(),
                        product.getName(),
                        entry.getValue(),
                        product.getPrice()));
            }
        }

        if (lines.isEmpty()) {
            request.getSession().setAttribute("shopError", "Không thể tạo đơn hàng từ giỏ hàng hiện tại.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        OrderStore.createOrder(getServletContext(), latestUser, lines, totalForLines(lines));

        CartStore.removeItems(getServletContext(), currentUser.getUsername(), new ArrayList<>(selectedItems.keySet()));

        request.getSession().setAttribute("shopSuccess", "Đặt hàng thành công. Cảm ơn bạn đã mua sắm!");
        response.sendRedirect(request.getContextPath() + "/orders");
    }

    private Map<String, Integer> getCart(String username) {
        return CartStore.getCartByUsername(getServletContext(), username);
    }

    private BigDecimal totalForLines(List<OrderStore.OrderLine> lines) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderStore.OrderLine line : lines) {
            total = total.add(line.getLineTotal());
        }
        return total;
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

        private final Product product;
        private final int quantity;
        private final BigDecimal lineTotal;

        public CartItemView(Product product, int quantity, BigDecimal lineTotal) {
            this.product = product;
            this.quantity = quantity;
            this.lineTotal = lineTotal;
        }

        public Product getProduct() {
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
