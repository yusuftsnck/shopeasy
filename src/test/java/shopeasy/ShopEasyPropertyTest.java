package shopeasy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

/**
 * Task 4 – Property-Based Testing (Chapter 5)
 *
 * <p>
 * Target classes: {@link PriceCalculator}, {@link ShoppingCart}
 *
 * <p>
 * Using jqwik, define and test at least <strong>3 distinct properties</strong>.
 * You must use at least one custom {@code @Provide} method.
 *
 * <h3>Suggested properties (you may use these or design your own)</h3>
 * <ul>
 * <li><b>Monotonicity</b> – For any fixed base and tax, increasing the discount
 * rate never increases the final price.</li>
 * <li><b>Identity</b> – A 0% discount and 0% tax returns exactly the base
 * price.</li>
 * <li><b>Boundedness</b> – The result is always &gt;= 0.</li>
 * <li><b>Cart commutativity</b> – Adding product A then B yields the same total
 * as adding B then A.</li>
 * <li><b>Discount transitivity</b> – Applying a 10% then another 10% discount
 * via {@code applyDiscount} is equivalent to a single call with the compounded
 * rate (think carefully: is this actually true for this implementation?).</li>
 * </ul>
 *
 * <h3>For each property, include a comment that answers:</h3>
 * <ol>
 * <li>What does this property mean in plain English?</li>
 * <li>What class of bugs would this property catch?</li>
 * </ol>
 *
 * <h3>If jqwik finds a failing case</h3>
 * Do not just fix the test. Investigate the root cause and explain it in your
 * reflection report (include the counterexample jqwik printed).
 */
class ShopEasyPropertyTest {

    // -----------------------------------------------------------------------
    // TODO: Write your properties below.
    // -----------------------------------------------------------------------
    /**
     * Property 1: Identity (a) What it means: Applying a 0% discount and 0% tax
     * to any valid base price should return exactly the original base price.
     * (b) Bug class caught: Catches errors in the core arithmetic formula
     * (e.g., adding/subtracting unexpected constants, off-by-one errors) that
     * corrupt the result even when rates are zero.
     */
    @Property
    void identityProperty(@ForAll @DoubleRange(min = 0.0, max = 10000.0) double basePrice) {
        PriceCalculator calc = new PriceCalculator();
        double result = calc.calculate(basePrice, 0.0, 0.0);
        assertThat(result).isCloseTo(basePrice, within(0.001));
    }

    /**
     * Property 2: Boundedness (a) What it means: For any valid base price,
     * discount, and tax, the final calculated price must be >= 0 and <=
     * basePrice * (1 + taxRate/100). (b) Bug class caught: Logic errors where
     * discounts accidentally increase the price, or taxes are applied
     * incorrectly, causing the final price to exceed logical boundaries.
     */
    @Property
    void boundednessProperty(
            @ForAll @DoubleRange(min = 0.0, max = 10000.0) double basePrice,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double discountRate,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double taxRate) {

        PriceCalculator calc = new PriceCalculator();
        double result = calc.calculate(basePrice, discountRate, taxRate);

        double maxPossiblePrice = basePrice * (1.0 + (taxRate / 100.0));

        // Using a small tolerance (0.0001) to prevent floating-point precision assertion failures
        assertThat(result).isGreaterThanOrEqualTo(0.0);
        assertThat(result).isLessThanOrEqualTo(maxPossiblePrice + 0.0001);
    }

    /**
     * Property 3: Cart Commutativity (a) What it means: The order in which
     * products are added to the shopping cart should not affect the final
     * total. Adding Product A then Product B must equal adding Product B then
     * Product A. (b) Bug class caught: State-dependent bugs where the cart
     * maintains an incorrect internal state based on insertion order, or if
     * subtotals are calculated improperly.
     */
    @Property
    void cartCommutativity(
            @ForAll("validProducts") Product productA,
            @ForAll("validProducts") Product productB,
            @ForAll @IntRange(min = 1, max = 10) int qtyA,
            @ForAll @IntRange(min = 1, max = 10) int qtyB) {

        ShoppingCart cart1 = new ShoppingCart();
        cart1.addItem(productA, qtyA);
        cart1.addItem(productB, qtyB);

        ShoppingCart cart2 = new ShoppingCart();
        cart2.addItem(productB, qtyB);
        cart2.addItem(productA, qtyA);

        assertThat(cart1.total()).isCloseTo(cart2.total(), within(0.001));
    }

    /**
     * Custom Provider for generating valid Product objects. Generates a unique
     * ID using java.util.UUID to prevent false-positive equality bugs.
     */
    @Provide
    Arbitrary<Product> validProducts() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
                Arbitraries.doubles().between(0.01, 500.0)
        ).as((name, price) -> new Product(java.util.UUID.randomUUID().toString(), name, price, 100));
    }

    // -----------------------------------------------------------------------
    //
    // EXAMPLE STRUCTURE:
    //
    // /**
    //  * Property: The final price is always non-negative.
    //  * Bug class caught: any implementation path that produces a negative result
    //  *                   (e.g., discount > 100 applied to negative base).
    //  */
    // @Property
    // void finalPriceIsNeverNegative(
    //         @ForAll @DoubleRange(min = 0, max = 10_000) double base,
    //         @ForAll @DoubleRange(min = 0, max = 100)   double discount,
    //         @ForAll @DoubleRange(min = 0, max = 100)   double tax) {
    //
    //     PriceCalculator calc = new PriceCalculator();
    //     double result = calc.calculate(base, discount, tax);
    //     assertThat(result).isGreaterThanOrEqualTo(0.0);
    // }
    //
    // // Custom provider example:
    // @Provide
    // Arbitrary<Product> validProducts() {
    //     return Combinators.combine(
    //             Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
    //             Arbitraries.doubles().between(0.01, 500.0)
    //     ).as((name, price) -> new Product("P-" + name, name, price, 100));
    // }
    // -----------------------------------------------------------------------
}
