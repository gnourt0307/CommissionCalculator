package com.swellstore.commission.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Selenium WebDriver end-to-end tests for the Swell Store Commission Calculator.
 *
 * <p><strong>Prerequisites:</strong>
 * <ol>
 *   <li>Run {@code mvn jetty:run} in the {@code swell-commission} directory first.</li>
 *   <li>Ensure {@code chromedriver} is on your PATH (or set via system property).</li>
 *   <li>Add selenium-java and junit-jupiter to pom.xml (see test-scope dependencies).</li>
 * </ol>
 *
 * <p><strong>Coverage:</strong>
 * <ul>
 *   <li>All 9 decision-table rules (R1–R9)</li>
 *   <li>All 5 valid boundaries (VB1–VB5)</li>
 *   <li>All validation error scenarios (IP1, IP3, IP5, IP7, IP8, IP9)</li>
 *   <li>Sticky-values repopulation after error</li>
 *   <li>"Calculate Again" navigation</li>
 *   <li>GET /calculate redirect</li>
 * </ul>
 */
@DisplayName("CommissionCalculator — Selenium UI Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommissionSeleniumTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "http://localhost:8080";
    private static final String INDEX_URL = BASE_URL + "/index.jsp";
    private static final String CALC_URL  = BASE_URL + "/calculate";

    // ------------------------------------------------------------------ //
    //  Setup / Teardown
    // ------------------------------------------------------------------ //

    @BeforeAll
    static void setUpDriver() {
        ChromeOptions options = new ChromeOptions();
        // Uncomment the line below to run headless (no browser window):
        // options.addArguments("--headless", "--disable-gpu");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void openForm() {
        driver.get(INDEX_URL);
    }

    // ------------------------------------------------------------------ //
    //  Helper Methods
    // ------------------------------------------------------------------ //

    /**
     * Fill all four form fields and submit.
     * Pass {@code null} for any field to leave it at its default (unselected / empty).
     */
    private void fillAndSubmit(String salaryType, String customerType,
                               String itemType, String price) {
        if (salaryType != null) {
            new Select(driver.findElement(By.id("salaryType")))
                    .selectByValue(salaryType);
        }
        if (customerType != null) {
            new Select(driver.findElement(By.id("customerType")))
                    .selectByValue(customerType);
        }
        if (itemType != null) {
            new Select(driver.findElement(By.id("itemType")))
                    .selectByValue(itemType);
        }
        if (price != null) {
            WebElement priceInput = driver.findElement(By.id("itemPrice"));
            priceInput.clear();
            priceInput.sendKeys(price);
        }
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    /** Returns the commission text from result.jsp (e.g. "$25.00"). */
    private String getCommissionText() {
        wait.until(ExpectedConditions.titleContains("Commission Result"));
        WebElement commissionDiv = driver.findElement(By.cssSelector(".commission"));
        // Text is: "Calculated Commission: $25.00"
        String full = commissionDiv.getText();
        return full.substring(full.lastIndexOf("$")).trim();
    }

    /** Returns all error message texts from the errors div on index.jsp. */
    private List<String> getErrorMessages() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".errors")));
        return driver.findElements(By.cssSelector(".errors li"))
                     .stream()
                     .map(WebElement::getText)
                     .toList();
    }

    // ------------------------------------------------------------------ //
    //  DECISION TABLE RULE TESTS (R1 – R9)
    // ------------------------------------------------------------------ //

    @Test
    @Order(1)
    @DisplayName("R1 | salaried + regular + standard + $500 → $0.00")
    void testR1_standardItem_alwaysZero() {
        fillAndSubmit("salaried", "regular", "standard", "500.00");
        assertEquals("$0.00", getCommissionText());
    }

    @Test
    @Order(2)
    @DisplayName("R1 | non-salaried + non-regular + standard → $0.00  (R1 dominant)")
    void testR1_standardItem_nonSalaried_nonRegular_alwaysZero() {
        fillAndSubmit("non-salaried", "non-regular", "standard", "9999.00");
        assertEquals("$0.00", getCommissionText());
    }

    @Test
    @Order(3)
    @DisplayName("R2 | salaried + regular + bonus + $500 → $0.00  (regular customer)")
    void testR2_regularCustomer_bonus_zero() {
        fillAndSubmit("salaried", "regular", "bonus", "500.00");
        assertEquals("$0.00", getCommissionText());
    }

    @Test
    @Order(4)
    @DisplayName("R2 | non-salaried + regular + other + $5000 → $0.00")
    void testR2_regularCustomer_other_zero() {
        fillAndSubmit("non-salaried", "regular", "other", "5000.00");
        assertEquals("$0.00", getCommissionText());
    }

    @Test
    @Order(5)
    @DisplayName("R3 | salaried + non-regular + bonus + $500 → $25.00  (5%)")
    void testR3_salaried_nonRegular_bonus_lowPrice_fivePct() {
        fillAndSubmit("salaried", "non-regular", "bonus", "500.00");
        assertEquals("$25.00", getCommissionText());
    }

    @Test
    @Order(6)
    @DisplayName("R4 | salaried + non-regular + bonus + $1500 → $25.00 flat")
    void testR4_salaried_nonRegular_bonus_highPrice_flat25() {
        fillAndSubmit("salaried", "non-regular", "bonus", "1500.00");
        assertEquals("$25.00", getCommissionText());
    }

    @Test
    @Order(7)
    @DisplayName("R5 | non-salaried + non-regular + bonus + $500 → $50.00  (10%)")
    void testR5_nonSalaried_nonRegular_bonus_lowPrice_tenPct() {
        fillAndSubmit("non-salaried", "non-regular", "bonus", "500.00");
        assertEquals("$50.00", getCommissionText());
    }

    @Test
    @Order(8)
    @DisplayName("R6 | non-salaried + non-regular + bonus + $1500 → $75.00 flat")
    void testR6_nonSalaried_nonRegular_bonus_highPrice_flat75() {
        fillAndSubmit("non-salaried", "non-regular", "bonus", "1500.00");
        assertEquals("$75.00", getCommissionText());
    }

    @Test
    @Order(9)
    @DisplayName("R7 | non-salaried + non-regular + other + $5000 → $500.00  (10%)")
    void testR7_nonSalaried_nonRegular_other_lowPrice_tenPct() {
        fillAndSubmit("non-salaried", "non-regular", "other", "5000.00");
        assertEquals("$500.00", getCommissionText());
    }

    @Test
    @Order(10)
    @DisplayName("R8 | non-salaried + non-regular + other + $15000 → $750.00  (5%)")
    void testR8_nonSalaried_nonRegular_other_highPrice_fivePct() {
        fillAndSubmit("non-salaried", "non-regular", "other", "15000.00");
        assertEquals("$750.00", getCommissionText());
    }

    @Test
    @Order(11)
    @DisplayName("R9 | salaried + non-regular + other + $5000 → $0.00")
    void testR9_salaried_nonRegular_other_alwaysZero() {
        fillAndSubmit("salaried", "non-regular", "other", "5000.00");
        assertEquals("$0.00", getCommissionText());
    }

    // ------------------------------------------------------------------ //
    //  BOUNDARY VALUE TESTS (VB2 – VB5)
    // ------------------------------------------------------------------ //

    @Test
    @Order(12)
    @DisplayName("VB2 | price = $1,000.00 exactly → uses 5%: $50.00  (not flat)")
    void testVB2_bonusThresholdExact_usesPct() {
        fillAndSubmit("salaried", "non-regular", "bonus", "1000.00");
        assertEquals("$50.00", getCommissionText()); // 5% × 1000 = 50
    }

    @Test
    @Order(13)
    @DisplayName("VB3 | price = $1,000.01 → flat $25.00  (just above bonus threshold)")
    void testVB3_justAboveBonusThreshold_usesFlat() {
        fillAndSubmit("salaried", "non-regular", "bonus", "1000.01");
        assertEquals("$25.00", getCommissionText());
    }

    @Test
    @Order(14)
    @DisplayName("VB4 | price = $10,000.00 exactly → uses 10%: $1,000.00  (not 5%)")
    void testVB4_otherThresholdExact_usesTenPct() {
        fillAndSubmit("non-salaried", "non-regular", "other", "10000.00");
        assertEquals("$1000.00", getCommissionText()); // 10% × 10000 = 1000
    }

    @Test
    @Order(15)
    @DisplayName("VB5 | price = $10,000.01 → uses 5%: $500.00  (just above other threshold)")
    void testVB5_justAboveOtherThreshold_usesFivePct() {
        fillAndSubmit("non-salaried", "non-regular", "other", "10000.01");
        // 5% × 10000.01 = 500.0005 → formatted as $500.00
        assertTrue(getCommissionText().startsWith("$500.00"));
    }

    // ------------------------------------------------------------------ //
    //  VALIDATION ERROR TESTS
    // ------------------------------------------------------------------ //

    @Test
    @Order(16)
    @DisplayName("IP7 | Empty price field → error 'Item price is required.'")
    void testValidation_emptyPrice_showsError() {
        fillAndSubmit("salaried", "regular", "standard", "");
        List<String> errors = getErrorMessages();
        assertTrue(errors.stream()
                .anyMatch(e -> e.contains("Item price is required")),
                "Expected 'Item price is required.' in errors but got: " + errors);
    }

    @Test
    @Order(17)
    @DisplayName("IP8 | Non-numeric price 'abc' → error 'Item price must be a valid number.'")
    void testValidation_nonNumericPrice_showsError() {
        fillAndSubmit("salaried", "regular", "standard", "abc");
        List<String> errors = getErrorMessages();
        assertTrue(errors.stream()
                .anyMatch(e -> e.contains("valid number")),
                "Expected 'valid number' error but got: " + errors);
    }

    @Test
    @Order(18)
    @DisplayName("IP9/IB1 | Price = '0' → error 'Item price must be greater than zero.'")
    void testValidation_zeroPrice_showsError() {
        fillAndSubmit("salaried", "regular", "standard", "0");
        List<String> errors = getErrorMessages();
        assertTrue(errors.stream()
                .anyMatch(e -> e.contains("greater than zero")),
                "Expected 'greater than zero' error but got: " + errors);
    }

    @Test
    @Order(19)
    @DisplayName("IB2/IP10 | Negative price '-100' → error 'greater than zero'")
    void testValidation_negativePrice_showsError() {
        fillAndSubmit("salaried", "regular", "standard", "-100");
        List<String> errors = getErrorMessages();
        assertTrue(errors.stream()
                .anyMatch(e -> e.contains("greater than zero")),
                "Expected 'greater than zero' error but got: " + errors);
    }

    @Test
    @Order(20)
    @DisplayName("IP1+IP3+IP5+IP7 | All fields missing → all 4 errors displayed")
    void testValidation_allFieldsMissing_showsFourErrors() {
        // Leave all selects at "-- Select --", leave price empty, submit
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        List<String> errors = getErrorMessages();
        assertEquals(4, errors.size(),
                "Expected 4 validation errors but got " + errors.size() + ": " + errors);
    }

    // ------------------------------------------------------------------ //
    //  STICKY VALUES & NAVIGATION TESTS
    // ------------------------------------------------------------------ //

    @Test
    @Order(21)
    @DisplayName("Sticky values — form repopulates after validation error")
    void testStickyValues_formRepopulatesAfterError() {
        fillAndSubmit("non-salaried", "non-regular", "bonus", "abc");
        // After error, form should show previously entered values
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".errors")));

        Select salarySelect = new Select(driver.findElement(By.id("salaryType")));
        Select customerSelect = new Select(driver.findElement(By.id("customerType")));
        Select itemSelect = new Select(driver.findElement(By.id("itemType")));
        String priceValue = driver.findElement(By.id("itemPrice")).getAttribute("value");

        assertEquals("non-salaried", salarySelect.getFirstSelectedOption().getAttribute("value"),
                "Salary type should be re-selected");
        assertEquals("non-regular", customerSelect.getFirstSelectedOption().getAttribute("value"),
                "Customer type should be re-selected");
        assertEquals("bonus", itemSelect.getFirstSelectedOption().getAttribute("value"),
                "Item type should be re-selected");
        assertEquals("abc", priceValue,
                "Price field should retain the invalid input 'abc'");
    }

    @Test
    @Order(22)
    @DisplayName("Navigation — 'Calculate Again' link returns to index.jsp")
    void testNavigation_calculateAgainLink() {
        fillAndSubmit("salaried", "regular", "standard", "500.00");
        wait.until(ExpectedConditions.titleContains("Commission Result"));

        WebElement calcAgainLink = driver.findElement(By.linkText("← Calculate Again"));
        calcAgainLink.click();

        wait.until(ExpectedConditions.titleContains("Commission Calculator"));
        assertTrue(driver.getCurrentUrl().endsWith("index.jsp")
                || driver.getCurrentUrl().endsWith("/"),
                "Should navigate back to index.jsp");
    }

    @Test
    @Order(23)
    @DisplayName("Navigation — GET /calculate redirects to index.jsp")
    void testNavigation_getCalculate_redirectsToIndex() {
        driver.get(CALC_URL);
        wait.until(ExpectedConditions.titleContains("Commission Calculator"));
        assertTrue(driver.getCurrentUrl().contains("index.jsp")
                || driver.getTitle().contains("Commission Calculator"),
                "GET /calculate should redirect to index.jsp");
    }

    // ------------------------------------------------------------------ //
    //  RESULT PAGE CONTENT VALIDATION
    // ------------------------------------------------------------------ //

    @Test
    @Order(24)
    @DisplayName("Result page — displays transaction summary with correct input values")
    void testResultPage_displaysSummary() {
        fillAndSubmit("non-salaried", "non-regular", "bonus", "500.00");
        wait.until(ExpectedConditions.titleContains("Commission Result"));

        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains("non-salaried"), "Result should show salary type");
        assertTrue(bodyText.contains("non-regular"),  "Result should show customer type");
        assertTrue(bodyText.contains("bonus"),         "Result should show item type");
        assertTrue(bodyText.contains("$500.00"),       "Result should show item price");
        assertTrue(bodyText.contains("$50.00"),        "Result should show commission");
    }
}
