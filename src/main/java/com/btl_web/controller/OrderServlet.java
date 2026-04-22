package com.btl_web.controller;

import com.btl_web.dao.OrderStoreDAO;
import com.btl_web.dao.UserDAO;
import com.btl_web.model.OrderStore;
import com.btl_web.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/orders")
public class OrderServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

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
        List<OrderStore.Order> orders = OrderStoreDAO.findByUsername(getServletContext(), currentUser.getUsername());
        OrderStore.OrderStatus selectedStatus = parseStatus(request.getParameter("status"));
        if (selectedStatus != null) {
            List<OrderStore.Order> filtered = new ArrayList<>();
            for (OrderStore.Order order : orders) {
                if (order.getStatus() == selectedStatus) {
                    filtered.add(order);
                }
            }
            orders = filtered;
        }

        request.setAttribute("profileUser", latestUser);
        request.setAttribute("orders", orders);
        request.setAttribute("selectedStatus", selectedStatus == null ? "ALL" : selectedStatus.name());
        request.getRequestDispatcher("/orders.jsp").forward(request, response);
    }

    private OrderStore.OrderStatus parseStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.trim().isEmpty()) {
            return null;
        }
        try {
            return OrderStore.OrderStatus.valueOf(rawStatus.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
