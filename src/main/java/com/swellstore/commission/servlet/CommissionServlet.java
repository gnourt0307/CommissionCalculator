package com.swellstore.commission.servlet;

import com.swellstore.commission.model.CommissionRequest;
import com.swellstore.commission.service.CommissionService;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles commission calculation requests.
 *
 * <ul>
 * <li>GET → redirect to {@code index.jsp}</li>
 * <li>POST → validate inputs, delegate to {@link CommissionService},
 * forward to {@code result.jsp} on success or back to
 * {@code index.jsp} with errors on failure.</li>
 * </ul>
 *
 * URL: {@code /calculate}
 */
public class CommissionServlet extends HttpServlet {

    // Valid option sets
    private static final List<String> VALID_SALARY_TYPES = List.of("salaried", "non-salaried");
    private static final List<String> VALID_CUSTOMER_TYPES = List.of("regular", "non-regular");
    private static final List<String> VALID_ITEM_TYPES = List.of("standard", "bonus", "other");

    /**
     * GET /calculate → redirect to the input form.
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }

    /**
     * POST /calculate → validate, calculate, and forward to the
     * appropriate view.
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // -------------------------------------------------------------- //
        // 1. Read parameters
        // -------------------------------------------------------------- //
        String salaryType = request.getParameter("salaryType");
        String customerType = request.getParameter("customerType");
        String itemType = request.getParameter("itemType");
        String priceStr = request.getParameter("itemPrice");

        // -------------------------------------------------------------- //
        // 2. Validate — collect all errors (not fail-fast)
        // -------------------------------------------------------------- //
        List<String> errors = new ArrayList<>();
        double itemPrice = 0.0;

        if (salaryType == null || !VALID_SALARY_TYPES.contains(salaryType)) {
            errors.add("Salary type is required and must be 'salaried' or 'non-salaried'.");
        }

        if (customerType == null || !VALID_CUSTOMER_TYPES.contains(customerType)) {
            errors.add("Customer type is required.");
        }

        if (itemType == null || !VALID_ITEM_TYPES.contains(itemType)) {
            errors.add("Item type is required.");
        }

        if (priceStr == null || priceStr.trim().isEmpty()) {
            errors.add("Item price is required.");
        } else {
            try {
                itemPrice = Double.parseDouble(priceStr.trim());
                if (itemPrice <= 0) {
                    errors.add("Item price must be greater than zero.");
                }
            } catch (NumberFormatException e) {
                errors.add("Item price must be a valid number.");
            }
        }

        // -------------------------------------------------------------- //
        // 3. On validation failure — forward back to index.jsp with errors
        // -------------------------------------------------------------- //
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            // Sticky values — re-populate the form with what the user typed
            request.setAttribute("prevSalaryType", salaryType);
            request.setAttribute("prevCustomerType", customerType);
            request.setAttribute("prevItemType", itemType);
            request.setAttribute("prevItemPrice", priceStr);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
            dispatcher.forward(request, response);
            return;
        }

        // -------------------------------------------------------------- //
        // 4. Validation passed — build request object and calculate
        // -------------------------------------------------------------- //
        CommissionRequest commissionRequest = new CommissionRequest(salaryType, customerType, itemType, itemPrice);

        double commission = CommissionService.calculate(commissionRequest);

        // -------------------------------------------------------------- //
        // 5. Forward to result.jsp with the result
        // -------------------------------------------------------------- //
        request.setAttribute("commission", commission);
        request.setAttribute("request", commissionRequest);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/result.jsp");
        dispatcher.forward(request, response);
    }
}
