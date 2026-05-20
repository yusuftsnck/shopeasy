package shopeasy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Task 1 – Specification-Based Testing (Chapter 2)
 *
 * <p>
 * Target class: {@link PriceCalculator}
 *
 * <p>
 * Your goal is to test
 * {@code PriceCalculator.calculate(basePrice, discountRate, taxRate)} using the
 * domain testing technique from Chapter 2:
 * <ol>
 * <li>Identify equivalence partitions for each input dimension.</li>
 * <li>Identify boundary values between partitions (on-point / off-point).</li>
 * <li>Write at least 10 meaningful test cases that cover both partitions and
 * boundaries.</li>
 * <li>Use {@code @ParameterizedTest} with {@code @CsvSource} for tests that
 * share structure.</li>
 * <li>Add a comment above each test method explaining which partition or
 * boundary it covers.</li>
 * </ol>
 *
 * <h3>Input dimensions to consider</h3>
 * <ul>
 * <li><b>basePrice</b> – zero, positive, very large</li>
 * <li><b>discountRate</b> – 0 (no discount), (0,100) typical, 100 (full
 * discount)</li>
 * <li><b>taxRate</b> – 0 (no tax), (0,100) typical, 100 (100% tax)</li>
 * </ul>
 */
class PriceCalculatorSpecTest {

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    // -----------------------------------------------------------------------
    // TODO: Write your tests below.
    // -----------------------------------------------------------------------
    //Partition: basePrice > 0, Typical discount and tax rates
    @ParameterizedTest(name = "base={0}, disc={1}%, tax={2}% => expected {3}")
    @CsvSource({
        "100.0, 10.0, 18.0, 106.2",
        "200.0, 15.0, 8.0, 183.6"
    })
    void typicalValues(double base, double disc, double tax, double expected) {
        assertThat(calculator.calculate(base, disc, tax)).isCloseTo(expected, within(0.001));
    }

    //Boundary (On-point): basePrice = 0.0 — result must always be 0 regardless of rates
    @Test
    void zeroBasePriceAlwaysReturnsZero() {
        assertThat(calculator.calculate(0.0, 20.0, 10.0)).isEqualTo(0.0);
    }

    //Boundary (On-point): discountRate = 0 (Lower bound) — no discount applied
    @Test
    void discountRateZeroMeansNoDiscount() {
        assertThat(calculator.calculate(100.0, 0.0, 10.0)).isEqualTo(110.0);
    }

    //Boundary (On-point): discountRate = 100 (Upper bound) — full discount wipes price to 0
    @Test
    void discountRateHundredMeansFullDiscount() {
        assertThat(calculator.calculate(100.0, 100.0, 10.0)).isEqualTo(0.0);
    }

    //Boundary (Off-point): discountRate = 1 (Just above lower bound)
    @Test
    void discountRateJustAboveZero() {
        assertThat(calculator.calculate(100.0, 1.0, 10.0)).isCloseTo(108.9, within(0.001));
    }

    //Boundary (Off-point): discountRate = 99 (Just below upper bound)
    @Test
    void discountRateJustBelowHundred() {
        assertThat(calculator.calculate(100.0, 99.0, 10.0)).isCloseTo(1.1, within(0.001));
    }

    //Boundary (On-point): taxRate = 0 (Lower bound) — no tax applied
    @Test
    void taxRateZeroMeansNoTax() {
        assertThat(calculator.calculate(100.0, 10.0, 0.0)).isEqualTo(90.0);
    }

    //Boundary (On-point): taxRate = 100 (Upper bound) — 100% tax applies duble on discounted price
    @Test
    void taxRateHundredMeansDoublePriceAfterDiscount() {
        assertThat(calculator.calculate(100.0, 10.0, 100.0)).isEqualTo(180.0);
    }

    //Boundary (Off-point): taxRate = 1 (Just above lower bound)
    @Test
    void taxRateJustAboveZero() {
        assertThat(calculator.calculate(100.0, 10.0, 1.0)).isCloseTo(90.9, within(0.001));
    }

    //Boundary (Off-point): taxRate = 99 (Just below upper bound)
    @Test
    void taxRateJustBelowHundred() {
        assertThat(calculator.calculate(100.0, 10.0, 99.0)).isCloseTo(179.1, within(0.001));
    }

    //Boundary (Off-point): Negative base price should violate pre-condition
    @ParameterizedTest(name = "Invalid inputs: base={0}, disc={1}%, tax={2}%")
    @CsvSource({
        "-10.0, 10.0, 10.0", // Negative base price
        "100.0, -5.0, 10.0", // Negative discount
        "100.0, 105.0, 10.0", // Discount > 100
        "100.0, 10.0, -5.0", // Negative tax
        "100.0, 10.0, 105.0" // Tax > 100
    })
    void testInvalidInputsShouldThrowAssertionError(double base, double disc, double tax) {
        assertThatThrownBy(() -> calculator.calculate(base, disc, tax))
                .isInstanceOf(AssertionError.class);
    }

    // -----------------------------------------------------------------------
    //
    // EXAMPLE STRUCTURE (replace with real cases):
    //
    // /** Partition: zero base price — result must always be 0 regardless of rates */
    // @Test
    // void zeroPriceAlwaysReturnsZero() {
    //     assertThat(calculator.calculate(0, 20, 10)).isEqualTo(0.0);
    // }
    //
    // /** Boundary: discountRate at lower bound (0%) — no reduction applied */
    // @Test
    // void discountRateZeroMeansNoDiscount() {
    //     double result = calculator.calculate(100, 0, 0);
    //     assertThat(result).isEqualTo(100.0);
    // }
    //
    // /** Boundary: discountRate at upper bound (100%) — full discount wipes price to 0 */
    // @Test
    // void discountRateHundredMeansFullDiscount() {
    //     double result = calculator.calculate(100, 100, 0);
    //     assertThat(result).isEqualTo(0.0);
    // }
    //
    // /** Partition: typical values — check formula correctness */
    // @ParameterizedTest(name = "base={0}, disc={1}%, tax={2}% => {3}")
    // @CsvSource({
    //     "100.0, 10.0, 20.0, 108.0",
    //     "200.0,  0.0, 10.0, 220.0",
    // })
    // void typicalValues(double base, double disc, double tax, double expected) {
    //     assertThat(calculator.calculate(base, disc, tax)).isCloseTo(expected, within(0.001));
    // }
    // -----------------------------------------------------------------------
}
