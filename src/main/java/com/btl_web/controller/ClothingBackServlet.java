package com.btl_web.controller;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/clothes/back")
public class ClothingBackServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        session.setAttribute("enteredProductCode", session.getAttribute("draftProductCode"));
        session.setAttribute("enteredName", session.getAttribute("draftName"));
        session.setAttribute("enteredCategory", session.getAttribute("draftCategory"));
        session.setAttribute("enteredSize", session.getAttribute("draftSize"));
        session.setAttribute("enteredColor", session.getAttribute("draftColor"));
        session.setAttribute("enteredPrice", session.getAttribute("draftPrice"));
        session.setAttribute("enteredStockQuantity", session.getAttribute("draftStockQuantity"));
        response.sendRedirect(request.getContextPath() + "/addproducts");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/addproducts");
    }
}
