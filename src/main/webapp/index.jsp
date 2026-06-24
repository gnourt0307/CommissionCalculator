<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Swell Store — Commission Calculator</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 520px;
            margin: 40px auto;
            padding: 0 16px;
        }
        h1 { font-size: 1.5rem; margin-bottom: 24px; }
        label { display: block; margin-top: 16px; font-weight: bold; }
        select, input[type="text"] {
            display: block;
            width: 100%;
            margin-top: 4px;
            padding: 6px 8px;
            font-size: 1rem;
            box-sizing: border-box;
        }
        .errors {
            border: 1px solid #c00;
            background: #fff0f0;
            padding: 10px 14px;
            margin-bottom: 16px;
            color: #c00;
        }
        .errors ul { margin: 0; padding-left: 18px; }
        button[type="submit"] {
            margin-top: 24px;
            padding: 8px 24px;
            font-size: 1rem;
            cursor: pointer;
        }
    </style>
</head>
<body>

<h1>Swell Store Commission Calculator</h1>

<%-- Display validation errors when present --%>
<%
    List<String> errors = (List<String>) request.getAttribute("errors");
    if (errors != null && !errors.isEmpty()) {
%>
    <div class="errors">
        <strong>Please correct the following errors:</strong>
        <ul>
<%      for (String error : errors) { %>
            <li><%= error %></li>
<%      } %>
        </ul>
    </div>
<%  } %>

<%-- Read sticky values (may be null on first load) --%>
<%
    String prevSalaryType   = (String) request.getAttribute("prevSalaryType");
    String prevCustomerType = (String) request.getAttribute("prevCustomerType");
    String prevItemType     = (String) request.getAttribute("prevItemType");
    String prevItemPrice    = (String) request.getAttribute("prevItemPrice");
    if (prevItemPrice == null) prevItemPrice = "";
%>

<form action="calculate" method="post">

    <label for="salaryType">Salesperson Type</label>
    <select id="salaryType" name="salaryType">
        <option value="" disabled <%= (prevSalaryType == null) ? "selected" : "" %>>-- Select --</option>
        <option value="salaried"     <%= "salaried".equals(prevSalaryType)     ? "selected" : "" %>>Salaried</option>
        <option value="non-salaried" <%= "non-salaried".equals(prevSalaryType) ? "selected" : "" %>>Non-Salaried</option>
    </select>

    <label for="customerType">Customer Type</label>
    <select id="customerType" name="customerType">
        <option value="" disabled <%= (prevCustomerType == null) ? "selected" : "" %>>-- Select --</option>
        <option value="regular"     <%= "regular".equals(prevCustomerType)     ? "selected" : "" %>>Regular</option>
        <option value="non-regular" <%= "non-regular".equals(prevCustomerType) ? "selected" : "" %>>Non-Regular</option>
    </select>

    <label for="itemType">Item Type</label>
    <select id="itemType" name="itemType">
        <option value="" disabled <%= (prevItemType == null) ? "selected" : "" %>>-- Select --</option>
        <option value="standard" <%= "standard".equals(prevItemType) ? "selected" : "" %>>Standard</option>
        <option value="bonus"    <%= "bonus".equals(prevItemType)    ? "selected" : "" %>>Bonus</option>
        <option value="other"    <%= "other".equals(prevItemType)    ? "selected" : "" %>>Other</option>
    </select>

    <label for="itemPrice">Item Price ($)</label>
    <input type="text" id="itemPrice" name="itemPrice"
           value="<%= prevItemPrice %>"
           placeholder="e.g. 500.00">

    <button type="submit">Calculate Commission</button>

</form>

</body>
</html>
