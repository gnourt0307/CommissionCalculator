package com.swellstore.commission.service;

import com.swellstore.commission.model.CommissionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JUnit 5 unit tests for {@link CommissionService}.
 *
 * Coverage:
 *   - Decision-table rules R1–R9 (spec §3)
 *   - Valid partitions VP1–VP10
 *   - Valid boundaries VB1–VB5
 *   - Rule-priority edge cases (R1 dominant, R2 dominant)
 *
 * Runs without a servlet container — CommissionService is pure static logic.
 */
@DisplayName("CommissionService — Decision Table Tests")
class CommissionServiceTest {

    private static final double DELTA = 0.001;

    /** Helper to avoid repeating new CommissionRequest(...) in every test. */
    private double calc(String salary, String customer, String item, double price) {
        return CommissionService.calculate(
                new CommissionRequest(salary, customer, item, price));
    }

    // RULE R1 — Standard item always yields $0.00
    // Covers: VP1, VP2, VP3, VP4, VP5, VP8, VP9, VP10

    @Test
    @DisplayName("R1 | salaried + regular + standard + $500 → $0.00")
    void r1_standardItem_salaried_regular_returnsZero() {
        assertEquals(0.0, calc("salaried", "regular", "standard", 500.0), DELTA);
    }

    @Test
    @DisplayName("R1 | non-salaried + non-regular + standard + $500 → $0.00  (R1 beats all)")
    void r1_standardItem_nonSalaried_nonRegular_returnsZero() {
        assertEquals(0.0, calc("non-salaried", "non-regular", "standard", 500.0), DELTA);
    }

    @Test
    @DisplayName("R1 | standard item with high price → still $0.00")
    void r1_standardItem_highPrice_returnsZero() {
        assertEquals(0.0, calc("non-salaried", "non-regular", "standard", 99999.0), DELTA);
    }

    // RULE R2 — Regular customer always yields $0.00  (non-standard items)
    // Covers: VP1, VP2, VP3, VP6, VP7, VP8, VP9

    @Test
    @DisplayName("R2 | salaried + regular + bonus + $500 → $0.00")
    void r2_regularCustomer_bonus_salaried_returnsZero() {
        assertEquals(0.0, calc("salaried", "regular", "bonus", 500.0), DELTA);
    }

    @Test
    @DisplayName("R2 | non-salaried + regular + other + $5000 → $0.00")
    void r2_regularCustomer_other_nonSalaried_returnsZero() {
        assertEquals(0.0, calc("non-salaried", "regular", "other", 5000.0), DELTA);
    }

    @Test
    @DisplayName("R2 | salaried + regular + other + high price → $0.00  (R2 beats other rules)")
    void r2_regularCustomer_other_highPrice_returnsZero() {
        assertEquals(0.0, calc("salaried", "regular", "other", 50000.0), DELTA);
    }

    // RULE R3 — Salaried + non-regular + bonus + price ≤ $1,000 → 5%
    // Covers: VP1, VP4, VP6, VP8, VB2

    @Test
    @DisplayName("R3 | salaried + non-regular + bonus + $500 → $25.00  (5%)")
    void r3_salaried_nonRegular_bonus_price500_returns5Pct() {
        assertEquals(25.0, calc("salaried", "non-regular", "bonus", 500.0), DELTA);
    }

    @Test
    @DisplayName("R3 | salaried + non-regular + bonus + $1.00 → $0.05  (5% of $1)")
    void r3_salaried_nonRegular_bonus_price1_returns0_05() {
        assertEquals(0.05, calc("salaried", "non-regular", "bonus", 1.0), DELTA);
    }

    @Test
    @DisplayName("R3 | VB2: price = $1,000.00 exactly → $50.00  (≤ boundary, uses %, not flat)")
    void vb2_salaried_nonRegular_bonus_price1000_returnsPercentage() {
        // 5% × 1000 = 50.00  — must NOT return $25.00 flat
        assertEquals(50.0, calc("salaried", "non-regular", "bonus", 1000.0), DELTA);
    }

    // RULE R4 — Salaried + non-regular + bonus + price > $1,000 → $25.00 flat
    // Covers: VP1, VP4, VP6, VP9, VB3

    @Test
    @DisplayName("R4 | salaried + non-regular + bonus + $1,500 → $25.00 flat")
    void r4_salaried_nonRegular_bonus_price1500_returnsFlat25() {
        assertEquals(25.0, calc("salaried", "non-regular", "bonus", 1500.0), DELTA);
    }

    @Test
    @DisplayName("R4 | VB3: price = $1,000.01 → $25.00 flat  (just above threshold)")
    void vb3_salaried_nonRegular_bonus_price1000_01_returnsFlat25() {
        assertEquals(25.0, calc("salaried", "non-regular", "bonus", 1000.01), DELTA);
    }

    @Test
    @DisplayName("R4 | very high price ($999,999) → still $25.00 flat")
    void r4_salaried_nonRegular_bonus_veryHighPrice_returnsFlat25() {
        assertEquals(25.0, calc("salaried", "non-regular", "bonus", 999999.0), DELTA);
    }

    // RULE R5 — Non-salaried + non-regular + bonus + price ≤ $1,000 → 10%
    // Covers: VP2, VP4, VP6, VP8, VB2

    @Test
    @DisplayName("R5 | non-salaried + non-regular + bonus + $500 → $50.00  (10%)")
    void r5_nonSalaried_nonRegular_bonus_price500_returns10Pct() {
        assertEquals(50.0, calc("non-salaried", "non-regular", "bonus", 500.0), DELTA);
    }

    @Test
    @DisplayName("R5 | non-salaried + non-regular + bonus + $1.00 → $0.10  (10% of $1)")
    void r5_nonSalaried_nonRegular_bonus_price1_returns0_10() {
        assertEquals(0.10, calc("non-salaried", "non-regular", "bonus", 1.0), DELTA);
    }

    @Test
    @DisplayName("R5 | VB2 (non-salaried): price = $1,000.00 exactly → $100.00  (10%, not flat)")
    void vb2_nonSalaried_nonRegular_bonus_price1000_returnsPercentage() {
        // 10% × 1000 = 100.00  — must NOT return $75.00 flat
        assertEquals(100.0, calc("non-salaried", "non-regular", "bonus", 1000.0), DELTA);
    }

    // RULE R6 — Non-salaried + non-regular + bonus + price > $1,000 → $75.00 flat
    // Covers: VP2, VP4, VP6, VP9, VB3

    @Test
    @DisplayName("R6 | non-salaried + non-regular + bonus + $1,500 → $75.00 flat")
    void r6_nonSalaried_nonRegular_bonus_price1500_returnsFlat75() {
        assertEquals(75.0, calc("non-salaried", "non-regular", "bonus", 1500.0), DELTA);
    }

    @Test
    @DisplayName("R6 | VB3 (non-salaried): price = $1,000.01 → $75.00 flat")
    void vb3_nonSalaried_nonRegular_bonus_price1000_01_returnsFlat75() {
        assertEquals(75.0, calc("non-salaried", "non-regular", "bonus", 1000.01), DELTA);
    }

    @Test
    @DisplayName("R6 | very high price ($999,999) → still $75.00 flat")
    void r6_nonSalaried_nonRegular_bonus_veryHighPrice_returnsFlat75() {
        assertEquals(75.0, calc("non-salaried", "non-regular", "bonus", 999999.0), DELTA);
    }

    // RULE R7 — Non-salaried + non-regular + other + price ≤ $10,000 → 10%
    // Covers: VP2, VP4, VP7, VP9, VB4

    @Test
    @DisplayName("R7 | non-salaried + non-regular + other + $5,000 → $500.00  (10%)")
    void r7_nonSalaried_nonRegular_other_price5000_returns10Pct() {
        assertEquals(500.0, calc("non-salaried", "non-regular", "other", 5000.0), DELTA);
    }

    @Test
    @DisplayName("R7 | VB4: price = $10,000.00 exactly → $1,000.00  (10%, not 5%)")
    void vb4_nonSalaried_nonRegular_other_price10000_returnsPercentage() {
        // 10% × 10000 = 1000.00  — must NOT return 5% = $500.00
        assertEquals(1000.0, calc("non-salaried", "non-regular", "other", 10000.0), DELTA);
    }

    @Test
    @DisplayName("R7 | VB1: minimum valid price $0.01 → 10% = $0.001 ≈ $0.00")
    void vb1_nonSalaried_nonRegular_other_minPrice_returnsAlmostZero() {
        assertEquals(0.001, calc("non-salaried", "non-regular", "other", 0.01), DELTA);
    }

    // RULE R8 — Non-salaried + non-regular + other + price > $10,000 → 5%
    // Covers: VP2, VP4, VP7, VP10, VB5

    @Test
    @DisplayName("R8 | non-salaried + non-regular + other + $15,000 → $750.00  (5%)")
    void r8_nonSalaried_nonRegular_other_price15000_returns5Pct() {
        assertEquals(750.0, calc("non-salaried", "non-regular", "other", 15000.0), DELTA);
    }

    @Test
    @DisplayName("R8 | VB5: price = $10,000.01 → $500.00  (5% of $10,000.01)")
    void vb5_nonSalaried_nonRegular_other_price10000_01_returns5Pct() {
        assertEquals(500.0005, calc("non-salaried", "non-regular", "other", 10000.01), DELTA);
    }

    @Test
    @DisplayName("R8 | very high price $1,000,000 → $50,000.00  (5%)")
    void r8_nonSalaried_nonRegular_other_veryHighPrice_returns5Pct() {
        assertEquals(50000.0, calc("non-salaried", "non-regular", "other", 1000000.0), DELTA);
    }

    // RULE R9 — Salaried + non-regular + other → $0.00 (policy silent)
    // Covers: VP1, VP4, VP7, VP8, VP9, VP10

    @Test
    @DisplayName("R9 | salaried + non-regular + other + $5,000 → $0.00")
    void r9_salaried_nonRegular_other_anyPrice_returnsZero() {
        assertEquals(0.0, calc("salaried", "non-regular", "other", 5000.0), DELTA);
    }

    @Test
    @DisplayName("R9 | salaried + non-regular + other + high price → still $0.00")
    void r9_salaried_nonRegular_other_highPrice_returnsZero() {
        assertEquals(0.0, calc("salaried", "non-regular", "other", 99999.0), DELTA);
    }

    // RULE-PRIORITY EDGE CASES

    @Test
    @DisplayName("Priority: R1 (standard) beats R2 (regular) — standard always wins")
    void rulePriority_r1_beats_r2_standard_regular() {
        // Both R1 and R2 would give $0.00, but important: R1 code path runs first
        assertEquals(0.0, calc("salaried", "regular", "standard", 500.0), DELTA);
    }

    @Test
    @DisplayName("Priority: R1 fires for standard item even with non-regular customer")
    void rulePriority_r1_fires_standard_nonRegular() {
        // Would otherwise enter R3/R5 territory if not standard
        assertEquals(0.0, calc("salaried", "non-regular", "standard", 500.0), DELTA);
    }

    @Test
    @DisplayName("Priority: R2 overrides bonus rules — regular customer always $0")
    void rulePriority_r2_overrides_bonusRules_regularCustomer() {
        // Non-salaried + regular + bonus would be R5/R6 territory, but R2 overrides
        assertEquals(0.0, calc("non-salaried", "regular", "bonus", 500.0), DELTA);
    }

    @Test
    void unknownItemType_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> CommissionService.calculate(
                        new CommissionRequest("salaried","non-regular","UNKNOWN",100.0)));
    }
}
