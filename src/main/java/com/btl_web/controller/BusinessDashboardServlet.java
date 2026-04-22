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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String servletPath = request.getServletPath();

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

        session.setAttribute("currentUser", latestUser);

        if ("/branch/dashboard".equals(servletPath)) {
            handleBranchOrderStatusUpdate(request, response, session, latestUser);
            return;
        }

        if (!userDAO.isCompanyOwner(latestUser) || !"/company/dashboard".equals(servletPath)) {
            response.sendRedirect(request.getContextPath() + "/shop");
            return;
        }

        UserDAO.OperationResult result = UserDAO.createBranchOwnerAccount(
                getServletContext(),
                normalize(request.getParameter("branchId")),
                normalize(request.getParameter("branchName")),
                normalize(request.getParameter("branchAddress")),
                normalize(request.getParameter("ownerUsername")),
                normalize(request.getParameter("ownerFullName")),
                normalize(request.getParameter("ownerPassword")));

        if (result.isSuccess()) {
            session.setAttribute("dashboardSuccess", result.getMessage());
        } else {
            session.setAttribute("dashboardError", result.getMessage());
            session.setAttribute("draftBranchId", normalize(request.getParameter("branchId")));
            session.setAttribute("draftBranchName", normalize(request.getParameter("branchName")));
            session.setAttribute("draftBranchAddress", normalize(request.getParameter("branchAddress")));
            session.setAttribute("draftOwnerUsername", normalize(request.getParameter("ownerUsername")));
            session.setAttribute("draftOwnerFullName", normalize(request.getParameter("ownerFullName")));
        }

        response.sendRedirect(request.getContextPath() + "/company/dashboard");
    }

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
        session.setAttribute("currentUser", latestUser);

        String servletPath = request.getServletPath();
        if ("/company/dashboard".equals(servletPath)) {
            if (!userDAO.isCompanyOwner(latestUser)) {
                response.sendRedirect(request.getContextPath() + "/shop");
                return;
            }
            request.setAttribute("companySummary", BusinessReportDAO.companySummary(getServletContext()));
            request.setAttribute("categoryStats", BusinessReportDAO.categoryStats(getServletContext()));
            request.setAttribute("branchStats", BusinessReportDAO.branchStats(getServletContext()));
            request.setAttribute("dashboardError", session.getAttribute("dashboardError"));
            request.setAttribute("dashboardSuccess", session.getAttribute("dashboardSuccess"));
            request.setAttribute("draftBranchId", session.getAttribute("draftBranchId"));
            request.setAttribute("draftBranchName", session.getAttribute("draftBranchName"));
            request.setAttribute("draftBranchAddress", session.getAttribute("draftBranchAddress"));
            request.setAttribute("draftOwnerUsername", session.getAttribute("draftOwnerUsername"));
            request.setAttribute("draftOwnerFullName", session.getAttribute("draftOwnerFullName"));
            session.removeAttribute("dashboardError");
            session.removeAttribute("dashboardSuccess");
            session.removeAttribute("draftBranchId");
            session.removeAttribute("draftBranchName");
            session.removeAttribute("draftBranchAddress");
            session.removeAttribute("draftOwnerUsername");
            session.removeAttribute("draftOwnerFullName");
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
        request.setAttribute(
                "pendingOrderCount",
                OrderStoreDAO.countByBranchAndStatus(
                        getServletContext(),
                        branchStat.getBranchId(),
                        OrderStore.OrderStatus.CHO_XAC_NHAN));
        request.setAttribute("branchSuccess", session.getAttribute("branchSuccess"));
        request.setAttribute("branchError", session.getAttribute("branchError"));
        session.removeAttribute("branchSuccess");
        session.removeAttribute("branchError");
        request.getRequestDispatcher("/branch-dashboard.jsp").forward(request, response);
    }

    private void handleBranchOrderStatusUpdate(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session,
            User latestUser) throws IOException {
        if (!userDAO.isBranchOwner(latestUser)) {
            response.sendRedirect(request.getContextPath() + "/shop");
            return;
        }

        String orderId = normalize(request.getParameter("orderId"));
        String action = normalize(request.getParameter("action"));
        if (orderId.isEmpty() || action.isEmpty()) {
            session.setAttribute("branchError", "Yêu cầu cập nhật đơn hàng không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/branch/dashboard");
            return;
        }

        OrderStore.OrderStatus fromStatus;
        OrderStore.OrderStatus toStatus;
        String successMessage;
        switch (action) {
            case "confirm":
                fromStatus = OrderStore.OrderStatus.CHO_XAC_NHAN;
                toStatus = OrderStore.OrderStatus.DA_XAC_NHAN;
                successMessage = "Đã xác nhận đơn hàng thành công.";
                break;
            case "ship":
                fromStatus = OrderStore.OrderStatus.DA_XAC_NHAN;
                toStatus = OrderStore.OrderStatus.DANG_GIAO;
                successMessage = "Đơn hàng đã được chuyển sang trạng thái đang giao.";
                break;
            case "deliver":
                fromStatus = OrderStore.OrderStatus.DANG_GIAO;
                toStatus = OrderStore.OrderStatus.DA_GIAO;
                successMessage = "Đã cập nhật đơn hàng giao thành công.";
                break;
            default:
                session.setAttribute("branchError", "Thao tác cập nhật trạng thái không hợp lệ.");
                response.sendRedirect(request.getContextPath() + "/branch/dashboard");
                return;
        }

        boolean updated = OrderStoreDAO.updateStatusByBranch(
                getServletContext(),
                latestUser.getBranchId(),
                orderId,
                fromStatus,
                toStatus);
        if (updated) {
            session.setAttribute("branchSuccess", successMessage);
        } else {
            session.setAttribute("branchError", "Không thể cập nhật đơn hàng. Vui lòng tải lại danh sách và thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/branch/dashboard");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
