package com.swellstore.commission.model;

/**
 * Immutable POJO carrying the validated inputs for a single commission
 * calculation request.  Lives only for the duration of one HTTP request.
 */
public class CommissionRequest {

    private final String salaryType;
    private final String customerType;
    private final String itemType;
    private final double itemPrice;

    /**
     * All-args constructor.
     *
     * @param salaryType   "salaried" or "non-salaried"
     * @param customerType "regular" or "non-regular"
     * @param itemType     "standard", "bonus", or "other"
     * @param itemPrice    Validated positive price
     */
    public CommissionRequest(String salaryType,
                             String customerType,
                             String itemType,
                             double itemPrice) {
        this.salaryType   = salaryType;
        this.customerType = customerType;
        this.itemType     = itemType;
        this.itemPrice    = itemPrice;
    }

    public String getSalaryType() {
        return salaryType;
    }

    public String getCustomerType() {
        return customerType;
    }

    public String getItemType() {
        return itemType;
    }

    public double getItemPrice() {
        return itemPrice;
    }
}
