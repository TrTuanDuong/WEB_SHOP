package com.btl_web.controller;

import com.btl_web.model.UserStore;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = { "/profile", "/profile/update", "/profile/address/add", "/profile/address/default",
        "/profile/address/update" })
public class ProfileServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserStore.User currentUser = requireLogin(request, response);
        if (currentUser == null) {
            return;
        }

        UserStore.User latest = UserStore.findByUsername(getServletContext(), currentUser.getUsername());
        request.setAttribute("profileUser", latest);
        request.getRequestDispatcher("/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        UserStore.User currentUser = requireLogin(request, response);
        if (currentUser == null) {
            return;
        }

        String path = request.getServletPath();
        if ("/profile/update".equals(path)) {
            updateProfile(request, response, currentUser);
            return;
        }

        if ("/profile/address/add".equals(path)) {
            addAddress(request, response, currentUser);
            return;
        }

        if ("/profile/address/default".equals(path)) {
            setDefaultAddress(request, response, currentUser);
            return;
        }

        if ("/profile/address/update".equals(path)) {
            updateAddress(request, response, currentUser);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void updateProfile(HttpServletRequest request, HttpServletResponse response, UserStore.User currentUser)
            throws IOException {
        UserStore.User latest = UserStore.findByUsername(getServletContext(), currentUser.getUsername());
        if (UserStore.isCheckoutProfileReady(latest)) {
            request.getSession().setAttribute("profileError",
                    "Thông tin cá nhân cố định đã được xác lập. Muốn sửa vui lòng liên hệ admin.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        String fullName = normalize(request.getParameter("fullName"));
        String ageText = normalize(request.getParameter("age"));
        String gender = normalize(request.getParameter("gender"));
        String email = normalize(request.getParameter("email"));
        String phone = normalize(request.getParameter("phone"));
        String baseAddress = normalize(request.getParameter("baseAddress"));

        if (fullName.isEmpty() || ageText.isEmpty() || gender.isEmpty() || email.isEmpty() || phone.isEmpty()
                || baseAddress.isEmpty()) {
            request.getSession().setAttribute("profileError", "Vui lòng nhập đầy đủ thông tin cá nhân cố định.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException exception) {
            request.getSession().setAttribute("profileError", "Tuổi phải là số nguyên hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        if (age < 1 || age > 120) {
            request.getSession().setAttribute("profileError", "Tuổi phải trong khoảng từ 1 đến 120.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            request.getSession().setAttribute("profileError", "Email không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        if (!phone.matches("[0-9]{9,11}")) {
            request.getSession().setAttribute("profileError", "Số điện thoại phải gồm 9-11 chữ số.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        if (!("Nam".equals(gender) || "Nữ".equals(gender) || "Khác".equals(gender))) {
            request.getSession().setAttribute("profileError", "Giới tính không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        UserStore.OperationResult result = UserStore.updateProfile(
                getServletContext(),
                currentUser.getUsername(),
                fullName,
                age,
                gender,
                email,
                phone,
                baseAddress);

        request.getSession().setAttribute(result.isSuccess() ? "profileSuccess" : "profileError", result.getMessage());
        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void addAddress(HttpServletRequest request, HttpServletResponse response, UserStore.User currentUser)
            throws IOException {
        String recipientName = normalize(request.getParameter("recipientName"));
        String recipientPhone = normalize(request.getParameter("recipientPhone"));
        String shippingAddress = normalize(request.getParameter("shippingAddress"));
        boolean setDefault = "on".equalsIgnoreCase(request.getParameter("setDefault"));

        if (recipientName.isEmpty() || recipientPhone.isEmpty() || shippingAddress.isEmpty()) {
            request.getSession().setAttribute("profileError", "Vui lòng nhập đủ thông tin địa chỉ giao hàng.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        if (!recipientPhone.matches("[0-9]{9,11}")) {
            request.getSession().setAttribute("profileError", "Số điện thoại người nhận phải gồm 9-11 chữ số.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        UserStore.OperationResult result = UserStore.addShippingAddress(
                getServletContext(),
                currentUser.getUsername(),
                recipientName,
                recipientPhone,
                shippingAddress,
                setDefault);

        request.getSession().setAttribute(result.isSuccess() ? "profileSuccess" : "profileError", result.getMessage());
        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void setDefaultAddress(HttpServletRequest request, HttpServletResponse response, UserStore.User currentUser)
            throws IOException {
        String addressId = normalize(request.getParameter("addressId"));
        UserStore.OperationResult result = UserStore.setDefaultAddress(
                getServletContext(),
                currentUser.getUsername(),
                addressId);
        request.getSession().setAttribute(result.isSuccess() ? "profileSuccess" : "profileError", result.getMessage());
        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void updateAddress(HttpServletRequest request, HttpServletResponse response, UserStore.User currentUser)
            throws IOException {
        String addressId = normalize(request.getParameter("addressId"));
        String recipientName = normalize(request.getParameter("recipientName"));
        String recipientPhone = normalize(request.getParameter("recipientPhone"));
        String shippingAddress = normalize(request.getParameter("shippingAddress"));
        boolean setDefault = "on".equalsIgnoreCase(request.getParameter("setDefault"));

        if (addressId.isEmpty() || recipientName.isEmpty() || recipientPhone.isEmpty() || shippingAddress.isEmpty()) {
            request.getSession().setAttribute("profileError", "Vui lòng nhập đầy đủ thông tin địa chỉ cần sửa.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        if (!recipientPhone.matches("[0-9]{9,11}")) {
            request.getSession().setAttribute("profileError", "Số điện thoại người nhận phải gồm 9-11 chữ số.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        UserStore.OperationResult result = UserStore.updateShippingAddress(
                getServletContext(),
                currentUser.getUsername(),
                addressId,
                recipientName,
                recipientPhone,
                shippingAddress,
                setDefault);

        request.getSession().setAttribute(result.isSuccess() ? "profileSuccess" : "profileError", result.getMessage());
        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private UserStore.User requireLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();
        UserStore.User currentUser = (UserStore.User) session.getAttribute("currentUser");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return null;
        }
        return currentUser;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
