package com.btl_web.controller;

import com.btl_web.dao.BusinessReportDAO;
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
import java.util.List;

@WebServlet(urlPatterns = { "/company/dashboard", "/branch/dashboard" })
public class BusinessDashboardServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

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
        if (latestUser == null) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String servletPath = request.getServletPath();
        if ("/company/dashboard".equals(servletPath)) {
            if (!userDAO.isCompanyOwner(latestUser)) {
                response.sendRedirect(request.getContextPath() + "/shop");
                return;
            }
            request.setAttribute("companySummary", BusinessReportDAO.companySummary(getServletContext()));
            request.setAttribute("branchStats", BusinessReportDAO.branchStats(getServletContext()));
            request.getRequestDispatcher("/company-dashboard.jsp").forward(request, response);
            return;
        }

        if (!userDAO.isBranchOwner(latestUser)) {
            response.sendRedirect(request.getContextPath() + "/shop");
            return;
        }

        BusinessReportDAO.BranchStat branchStat = BusinessReportDAO.branchStatByOwner(
                getServletContext(),
                latestUser.getUsername());
        if (branchStat == null) {
            request.setAttribute("branchError", "Tài khoản chưa được gán quyền sở hữu chi nhánh.");
            request.getRequestDispatcher("/branch-dashboard.jsp").forward(request, response);
            return;
        }

        List<OrderStore.Order> orders = OrderStoreDAO.findByBranchId(getServletContext(), branchStat.getBranchId(), 20);
        request.setAttribute("branchStat", branchStat);
        request.setAttribute("orders", orders);
        request.getRequestDispatcher("/branch-dashboard.jsp").forward(request, response);
    }
}
