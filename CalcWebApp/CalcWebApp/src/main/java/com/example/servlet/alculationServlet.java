package com.example.servlet;

import com.example.dao.CalculationDAO;
import com.example.model.Calculation;
import com.example.util.CalcUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * Quick calculation straight from the browser address bar.
 * Example:  http://localhost:8080/cal?a=10&op=ADD&b=5
 * The result is shown as plain text and saved to the database.
 */
@WebServlet("/cal")
public class alculationServlet extends HttpServlet {

    private final CalculationDAO dao = new CalculationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {

            String aParam = req.getParameter("a");
            if (aParam == null) {
                out.println("Usage: /cal?a=10&op=ADD&b=5");
                out.println("op  : ADD SUB MUL DIV MOD POW  (needs a and b)");
                out.println("      SQRT ABS FACT           (needs only a)");
                return;
            }

            String op = req.getParameter("op");
            if (op == null) op = "ADD";
            op = op.trim().toUpperCase();

            if (!CalcUtil.isValidOp(op)) {
                resp.setStatus(400);
                out.println("Invalid op. Use: ADD SUB MUL DIV MOD POW SQRT ABS FACT");
                return;
            }

            try {
                double a = Double.parseDouble(aParam.trim());
                Double b = null;
                if (CalcUtil.needsSecondNumber(op)) {
                    String bParam = req.getParameter("b");
                    if (bParam == null) {
                        resp.setStatus(400);
                        out.println("Parameter b is required for " + op);
                        return;
                    }
                    b = Double.parseDouble(bParam.trim());
                }

                CalcUtil.Result res = CalcUtil.calculate(op, a, b);
                dao.save(new Calculation(a, b, op, res.value, res.error));

                if (res.error != null) {
                    out.println("Error: " + res.error);
                } else {
                    out.println(CalcUtil.describe(op, a, b) + " = " + CalcUtil.format(res.value));
                }
                out.println("(saved to database)");

            } catch (NumberFormatException e) {
                resp.setStatus(400);
                out.println("a and b must be valid numbers.");
            } catch (SQLException e) {
                resp.setStatus(500);
                out.println("Database error: " + e.getMessage());
            }
        }
    }
}
