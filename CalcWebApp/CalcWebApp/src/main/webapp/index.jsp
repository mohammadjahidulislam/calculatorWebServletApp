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
