package com.swellstore.commission.service;

import com.swellstore.commission.model.CommissionRequest;

/**
 * Pure business-logic service implementing the Swell Store commission
 * decision table (Sections 3 and 7 of the specification).
 *
 * <p>This class is stateless.  The single public method is static and
 * produces no side effects — same inputs always return the same output.</p>
 *
 * <p>Decision-table rules (R1–R9):
 * <pre>
 *  R1  Any        Any         standard  —              $0.00
 *  R2  Any        regular     Any       —              $0.00
 *  R3  salaried   non-regular bonus     price ≤ 1000   5% of price
 *  R4  salaried   non-regular bonus     price > 1000   $25.00 flat
 *  R5  non-sal.   non-regular bonus     price ≤ 1000   10% of price
 *  R6  non-sal.   non-regular bonus     price > 1000   $75.00 flat
 *  R7  non-sal.   non-regular other     price ≤ 10000  10% of price
 *  R8  non-sal.   non-regular other     price > 10000  5% of price
 *  R9  salaried   non-regular other     —              $0.00
 * </pre>
 * </p>
 */
public class CommissionService {

    // ------------------------------------------------------------------ //
    //  Named constants for thresholds and commission rates (spec §11)
    // ------------------------------------------------------------------ //

    private static final double BONUS_PRICE_THRESHOLD  = 1000.0;
    private static final double OTHER_PRICE_THRESHOLD  = 10000.0;
    private static final double SALARIED_BONUS_PCT     = 0.05;
    private static final double SALARIED_BONUS_FLAT    = 25.0;
    private static final double NONSALARIED_BONUS_PCT  = 0.10;
    private static final double NONSALARIED_BONUS_FLAT = 75.0;
    private static final double NONSALARIED_OTHER_LOW  = 0.10;
    private static final double NONSALARIED_OTHER_HIGH = 0.05;

    // Utility class — no instantiation needed.
    private CommissionService() {}

    /**
     * Calculates the commission for a validated transaction request.
     *
     * <p>Pre-condition: {@code req} contains only values that have already
     * passed servlet-level validation; no further defensive checks are
     * needed (per spec §7).</p>
     *
     * @param req validated commission request
     * @return commission amount as a {@code double} (≥ 0.0)
     */
    public static double calculate(CommissionRequest req) {

        // ---- R1: Standard item → always $0.00 -------------------------
        if ("standard".equals(req.getItemType())) {
            return 0.0;
        }

        // ---- R2: Regular customer → always $0.00 ----------------------
        if ("regular".equals(req.getCustomerType())) {
            return 0.0;
        }

        // From here: non-regular customer + non-standard item

        if ("bonus".equals(req.getItemType())) {

            if ("salaried".equals(req.getSalaryType())) {
                // R3: salaried, bonus, price ≤ 1000 → 5%
                if (req.getItemPrice() <= BONUS_PRICE_THRESHOLD) {
                    return req.getItemPrice() * SALARIED_BONUS_PCT;
                }
                // R4: salaried, bonus, price > 1000 → $25.00 flat
                else {
                    return SALARIED_BONUS_FLAT;
                }

            } else {
                // non-salaried
                // R5: non-salaried, bonus, price ≤ 1000 → 10%
                if (req.getItemPrice() <= BONUS_PRICE_THRESHOLD) {
                    return req.getItemPrice() * NONSALARIED_BONUS_PCT;
                }
                // R6: non-salaried, bonus, price > 1000 → $75.00 flat
                else {
                    return NONSALARIED_BONUS_FLAT;
                }
            }
        }

        if ("other".equals(req.getItemType())) {

            if ("non-salaried".equals(req.getSalaryType())) {
                // R7: non-salaried, other, price ≤ 10000 → 10%
                if (req.getItemPrice() <= OTHER_PRICE_THRESHOLD) {
                    return req.getItemPrice() * NONSALARIED_OTHER_LOW;
                }
                // R8: non-salaried, other, price > 10000 → 5%
                else {
                    return req.getItemPrice() * NONSALARIED_OTHER_HIGH;
                }

            } else {
                // salaried
                // R9: salaried, other, non-regular → $0.00 (policy silent)
                return 0.0;
            }
        } else {
            throw new IllegalArgumentException(
                    "Unknown item type: " + req.getItemType());
        }
    }
}
