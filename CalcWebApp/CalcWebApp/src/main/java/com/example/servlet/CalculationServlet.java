package com.example.servlet;

import com.example.dao.CalculationDAO;
import com.example.model.Calculation;
import com.example.util.CalcUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;

@WebServlet("/calc")
public class CalculationServlet extends HttpServlet {

    private final CalculationDAO dao = new CalculationDAO();

    // show the page: form + saved calculations
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        loadRecords(req);
        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }

    // calculate, save to the database (or clear the history)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String message;

        if ("clear".equals(req.getParameter("action"))) {
            try {
                dao.clearAll();
                message = "History cleared.";
            } catch (SQLException e) {
                message = "Database error: " + e.getMessage();
            }
        } else {
            message = calculateAndSave(req);
        }

        req.setAttribute("message", message);
        loadRecords(req);
        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }

    private String calculateAndSave(HttpServletRequest req) {
        String op = req.getParameter("op");
        if (!CalcUtil.isValidOp(op)) {
            return "Please choose a valid operation.";
        }

        try {
            double a = Double.parseDouble(req.getParameter("a").trim());
            Double b = null;
            if (CalcUtil.needsSecondNumber(op)) {
                b = Double.parseDouble(req.getParameter("b").trim());
            }

            CalcUtil.Result res = CalcUtil.calculate(op, a, b);
            dao.save(new Calculation(a, b, op, res.value, res.error));

            String expr = CalcUtil.describe(op, a, b);
            if (res.error != null) {
                return "Error: " + res.error + "  (saved to database)";
            }
            return "Result: " + expr + " = " + CalcUtil.format(res.value) + "  (saved to database)";

        } catch (NumberFormatException | NullPointerException e) {
            return "Please enter valid numbers.";
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    private void loadRecords(HttpServletRequest req) {
        try {
            req.setAttribute("records", dao.findRecent(30));
        } catch (SQLException e) {
            req.setAttribute("records", Collections.<Calculation>emptyList());
            req.setAttribute("dbError", e.getMessage());
        }
    }
}
