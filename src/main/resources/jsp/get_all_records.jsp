<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, java.util.*" %>
<%
Class.forName("com.mysql.jdbc.Driver");
try (Connection conn = DriverManager.getConnection(
        "jdbc:mysql://db:3306/easybuggy?useSSL=false", "admin", "password")) {

    // Get a list of tables
    List<String> tables = new ArrayList<>();
    try (PreparedStatement ps = conn.prepareStatement("SHOW TABLES");
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            tables.add(rs.getString(1));
        }
    }
%>
<html>
<head>
<style>
th, td { border: solid #ccc; padding: 4px; }
table { margin-bottom: 10px; border-collapse: collapse; }
</style>
</head>
<body>
<%
    // Get all data from each table
    for (String table : tables) {
        out.println("<h2>" + table + " table</h2>");

        String sql = "SELECT * FROM `" + table + "`";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
%>
<table>
    <thead><tr>
    <% for (int i = 1; i <= cols; i++) { %>
        <th><%= md.getColumnLabel(i) %></th>
    <% } %>
    </tr></thead>
    <tbody>
    <% while (rs.next()) { %>
        <tr>
        <% for (int i = 1; i <= cols; i++) { %>
            <td><%= rs.getObject(i) %></td>
        <% } %>
        </tr>
    <% } %>
    </tbody>
</table>
<%
        } catch (Exception e) {
            out.println("<p>Error reading table " + table + ": " + e.getMessage() + "</p>");
        }
%>
</body>
</html>
<%
    }
} catch (Exception e) {
    out.println("<p class='error'>Error: " + e.getMessage() + "</p>");
}
%>