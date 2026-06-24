<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.swellstore.commission.model.CommissionRequest" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Swell Store — Commission Result</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 520px;
            margin: 40px auto;
            padding: 0 16px;
        }
        h1 { font-size: 1.5rem; margin-bottom: 24px; }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 24px;
        }
        th, td {
            text-align: left;
            padding: 8px 10px;
            border: 1px solid #ccc;
        }
        th { background: #f0f0f0; width: 40%; }
        .commission {
            font-size: 1.4rem;
            font-weight: bold;
            margin-bottom: 24px;
        }
        a { color: #0056b3; }
    </style>
</head>
<body>

<h1>Commission Result</h1>

<%
    CommissionRequest req = (CommissionRequest) request.getAttribute("request");
    Double commission     = (Double) request.getAttribute("commission");
%>

<h2>Transaction Summary</h2>
<table>
    <tr>
        <th>Salesperson Type</th>
        <td><%= req.getSalaryType() %></td>
    </tr>
    <tr>
        <th>Customer Type</th>
        <td><%= req.getCustomerType() %></td>
    </tr>
    <tr>
        <th>Item Type</th>
        <td><%= req.getItemType() %></td>
    </tr>
    <tr>
        <th>Item Price</th>
        <td><%= String.format("$%.2f", req.getItemPrice()) %></td>
    </tr>
</table>

<div class="commission">
    Calculated Commission: <%= String.format("$%.2f", commission) %>
</div>

<a href="index.jsp">&#8592; Calculate Again</a>

</body>
</html>
