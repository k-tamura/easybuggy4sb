<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, javax.naming.*, javax.sql.DataSource, java.util.*" %>
<%  Class.forName("com.mysql.jdbc.Driver");
    try (Connection conn = DriverManager.getConnection("jdbc:mysql://db:3306/easybuggy?useSSL=false", "admin", "password");
         PreparedStatement ps = conn.prepareStatement("SELECT * FROM users");
         ResultSet rs = ps.executeQuery()) {
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount(); %>
<html>
<head>
    <style>th, td { border: solid #ccc; }</style>
</head>
<body>
<h2>users table</h2>
<table>
    <thead><tr>
    <% for (int i = 1; i <= cols; i++) { String colName = md.getColumnLabel(i);  %>
        <th><%= colName %></th>
    <% } %>
    </tr></thead>
    <tbody>
    <% while (rs.next()) { %>
        <tr><% for (int i = 1; i <= cols; i++) { %>
            <td><%= rs.getObject(i) %></td>
        <% } %></tr>
    <% } %>
    </tbody>
</table>
</body>
</html>
<% } catch (Exception e) { out.println("<p class='error'>Error: " + e.getMessage() + "</p>"); } %>
