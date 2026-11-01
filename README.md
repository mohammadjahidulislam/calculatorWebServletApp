Name: Md Jahidul Islam




ID: IT24018






TAsk Name : Calculator Web Servlet App





CalculationDAO Code:





package com.example.dao;

import com.example.model.Calculation;
import com.example.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CalculationDAO {

    /** Saves one calculation (successful or with an error message). */
    public void save(Calculation c) throws SQLException {
        StrAing sql = "INSERT INTO calculation (operand1, operand2, operation, result, error_msg) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, c.getOperand1());
            if (c.getOperand2() == null) ps.setNull(2, Types.DOUBLE); else ps.setDouble(2, c.getOperand2());
            ps.setString(3, c.getOperation());
            if (c.getResult() == null) ps.setNull(4, Types.DOUBLE); else ps.setDouble(4, c.getResult());
            ps.setString(5, c.getErrorMsg());
            ps.executeUpdate();
        }
    }

    /** Most recent calculations first. */
    public List<Calculation> findRecent(int limit) throws SQLException {
        List<Calculation> list = new ArrayList<>();
        String sql = "SELECT id, operand1, operand2, operation, result, error_msg, created_at " +
                     "FROM calculation ORDER BY id DESC LIMIT ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Calculation c = new Calculation();
                    c.setId(rs.getLong("id"));
                    c.setOperand1(rs.getDouble("operand1"));
                    double o2 = rs.getDouble("operand2");
                    c.setOperand2(rs.wasNull() ? null : o2);
                    c.setOperation(rs.getString("operation"));
                    double res = rs.getDouble("result");
                    c.setResult(rs.wasNull() ? null : res);
                    c.setErrorMsg(rs.getString("error_msg"));
                    c.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(c);
                }
            }
        }
        return list;
    }

    /** Deletes every saved calculation. */
    public void clearAll() throws SQLException {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate("TRUNCATE TABLE calculation");
        }
    }
}





Camculation Code:




package com.example.model;

import java.sql.Timestamp;

/** One row of the calculation table. */
public class Calculation {

    private long id;
    private double operand1;
    private Double operand2;     // null for single-number operations
    private String operation;
    private Double result;       // null when there was an error
    private String errorMsg;     // null when the calculation succeeded
    private Timestamp createdAt;

    public Calculation() { }

    public Calculation(double operand1, Double operand2, String operation, Double result, String errorMsg) {
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operation = operation;
        this.result = result;
        this.errorMsg = errorMsg;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public double getOperand1() { return operand1; }
    public void setOperand1(double operand1) { this.operand1 = operand1; }

    public Double getOperand2() { return operand2; }
    public void setOperand2(Double operand2) { this.operand2 = operand2; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public Double getResult() { return result; }
    public void setResult(Double result) { this.result = result; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}





**Calculation Servlet Code:






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





**CalcUtil Code:
**





package com.example.util;

import java.util.Set;

public class CalcUtil {

    /** Operations that need two numbers. */
    public static final Set<String> BINARY = Set.of("ADD", "SUB", "MUL", "DIV", "MOD", "POW");

    /** Operations that need one number. */
    public static final Set<String> UNARY = Set.of("SQRT", "ABS", "FACT");

    private CalcUtil() { }

    /** Holds either a value or an error message. */
    public static class Result {
        public final Double value;
        public final String error;

        public Result(Double value, String error) {
            this.value = value;
            this.error = error;
        }
    }

    public static boolean isValidOp(String op) {
        return op != null && (BINARY.contains(op) || UNARY.contains(op));
    }

    public static boolean needsSecondNumber(String op) {
        return BINARY.contains(op);
    }

    /** All the math lives here. b is null for single-number operations. */
    public static Result calculate(String op, double a, Double b) {
        switch (op) {
            case "ADD": return new Result(a + b, null);
            case "SUB": return new Result(a - b, null);
            case "MUL": return new Result(a * b, null);
            case "DIV":
                if (b == 0) return new Result(null, "Division by zero");
                return new Result(a / b, null);
            case "MOD":
                if (b == 0) return new Result(null, "Modulo by zero");
                return new Result(a % b, null);
            case "POW":
                double p = Math.pow(a, b);
                if (Double.isNaN(p) || Double.isInfinite(p))
                    return new Result(null, "Invalid power result");
                return new Result(p, null);
            case "SQRT":
                if (a < 0) return new Result(null, "Square root of a negative number");
                return new Result(Math.sqrt(a), null);
            case "ABS":
                return new Result(Math.abs(a), null);
            case "FACT":
                if (a < 0 || a != Math.floor(a) || a > 20)
                    return new Result(null, "Factorial: enter a whole number from 0 to 20");
                double f = 1;
                for (int i = 2; i <= (int) a; i++) f *= i;
                return new Result(f, null);
            default:
                return new Result(null, "Unknown operation: " + op);
        }
    }

    /** Symbol used when showing an expression, e.g. ADD -> "+". */
    public static String symbol(String op) {
        switch (op) {
            case "ADD": return "+";
            case "SUB": return "-";
            case "MUL": return "*";
            case "DIV": return "/";
            case "MOD": return "%";
            case "POW": return "^";
            default:    return op;
        }
    }

    /** Console helper: "+" -> "ADD", "sqrt" -> "SQRT", etc. Returns null if unknown. */
    public static String fromSymbol(String s) {
        if (s == null) return null;
        switch (s.trim().toLowerCase()) {
            case "+":    return "ADD";
            case "-":    return "SUB";
            case "*":    return "MUL";
            case "/":    return "DIV";
            case "%":    return "MOD";
            case "^":    return "POW";
            case "sqrt": return "SQRT";
            case "abs":  return "ABS";
            case "fact": return "FACT";
            default:     return null;
        }
    }

    /** 15.0 -> "15", 2.5 -> "2.5" */
    public static String format(double d) {
        if (d == Math.rint(d) && Math.abs(d) < 1e15) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }

    /** Human readable expression, e.g. "10 + 5" or "SQRT(144)". */
    public static String describe(String op, double a, Double b) {
        if (BINARY.contains(op) && b != null) {
            return format(a) + " " + symbol(op) + " " + format(b);
        }
        return op + "(" + format(a) + ")";
    }
}







**DBConnection Code:**






package com.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL  = "jdbc:mysql://localhost:3306/calcdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "Jahid@ict21";   // <-- put your MySQL password here

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL driver not found", e);
        }
    }

    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}








**Index.Jsp**




<%@ page contentType="text/html;charset=UTF-8" language="java"
         import="java.util.List, com.example.model.Calculation, com.example.util.CalcUtil" %>
<%!
    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
%>
<%
    // opening index.jsp directly redirects to /calc so the data gets loaded
    if (request.getAttribute("records") == null) {
        response.sendRedirect(request.getContextPath() + "/calc");
        return;
    }
    @SuppressWarnings("unchecked")
    List<Calculation> records = (List<Calculation>) request.getAttribute("records");
    String message = (String) request.getAttribute("message");
    String dbError = (String) request.getAttribute("dbError");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>JDBC Calculator</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        form { margin: 10px 0; }
        input, select, button { padding: 6px; font-size: 15px; }
        table { border-collapse: collapse; margin-top: 10px; }
        td, th { border: 1px solid #888; padding: 6px 16px; text-align: center; }
        th { background: #eee; }
        .msg { margin: 14px 0; font-weight: bold; color: #0a5; }
        .err { margin: 14px 0; color: #c00; }
    </style>
</head>
<body>
    <h1>Calculator Testing System!</h1>
    <h3>Welcome to My Calculator Web Application</h3>

    <h2>Calculator</h2>
    <form method="post" action="<%= request.getContextPath() %>/calc">
        <input type="text" name="a" placeholder="First number" required>
        <select name="op" id="op" onchange="toggleSecond()">
            <option value="ADD">+ Add</option>
            <option value="SUB">- Subtract</option>
            <option value="MUL">* Multiply</option>
            <option value="DIV">/ Divide</option>
            <option value="MOD">% Modulus</option>
            <option value="POW">^ Power</option>
            <option value="SQRT">Square root</option>
            <option value="ABS">Absolute value</option>
            <option value="FACT">Factorial</option>
        </select>
        <input type="text" name="b" id="b" placeholder="Second number">
        <button type="submit">Calculate &amp; Save</button>
    </form>

    <% if (message != null) { %>
        <div class="msg"><%= esc(message) %></div>
    <% } %>
    <% if (dbError != null) { %>
        <div class="err">Database error: <%= esc(dbError) %></div>
    <% } %>

    <h3>Saved in database (latest 30)</h3>
    <table>
        <tr><th>ID</th><th>Calculation</th><th>Result</th><th>Error</th><th>Time</th></tr>
        <% if (records.isEmpty()) { %>
            <tr><td colspan="5">No calculations yet.</td></tr>
        <% } %>
        <% for (Calculation c : records) { %>
            <tr>
                <td><%= c.getId() %></td>
                <td><%= esc(CalcUtil.describe(c.getOperation(), c.getOperand1(), c.getOperand2())) %></td>
                <td><%= c.getResult() == null ? "-" : CalcUtil.format(c.getResult()) %></td>
                <td><%= c.getErrorMsg() == null ? "-" : esc(c.getErrorMsg()) %></td>
                <td><%= c.getCreatedAt() %></td>
            </tr>
        <% } %>
    </table>

    <form method="post" action="<%= request.getContextPath() %>/calc"
          onsubmit="return confirm('Delete all history?');">
        <input type="hidden" name="action" value="clear">
        <button type="submit">Clear history</button>
    </form>

    <p>Quick test: <a href="<%= request.getContextPath() %>/cal?a=10&amp;op=ADD&amp;b=5">/cal?a=10&amp;op=ADD&amp;b=5</a></p>

    <script>
        // hide the second number box for single-number operations
        function toggleSecond() {
            var op = document.getElementById('op').value;
            var single = (op === 'SQRT' || op === 'ABS' || op === 'FACT');
            var b = document.getElementById('b');
            b.style.display = single ? 'none' : '';
            b.required = !single;
            if (single) b.value = '';
        }
        toggleSecond();
    </script>
</body>
</html>








**OUTPUT:******



<img width="425" height="551" alt="Screenshot 2026-11-01 203412" src="https://github.com/user-attachments/assets/ace315bd-2abb-4d39-8296-09c7395ada0f" />











<img width="802" height="442" alt="Screenshot 2026-11-01 203523" src="https://github.com/user-attachments/assets/2fcad474-66f2-42b1-857a-21859c7bbbf2" />










<img width="917" height="843" alt="Screenshot 2026-11-01 210707" src="https://github.com/user-attachments/assets/c33edfa0-d2a1-4fa1-b6a2-bdce3a130bff" />


