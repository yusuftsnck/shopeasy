package shopeasy;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Task 3 – Design by Contract (Chapter 4)
 *
 * <p>
 * This task has two parts:
 *
 * <h3>Part A – Add contracts to production code</h3>
 * Open {@link ShoppingCart} and {@link PriceCalculator} and add {@code assert}
 * statements for the pre-conditions and post-conditions described in their
 * Javadoc. Note: assertions are enabled via {@code -ea} in Maven Surefire
 * (already configured in {@code pom.xml}).
 *
 * <p>
 * Contracts to implement:
 * <ul>
 * <li><b>ShoppingCart.addItem</b>: pre — {@code product != null},
 * {@code quantity > 0}; post — {@code itemCount()} increased or product
 * quantity updated.</li>
 * <li><b>ShoppingCart.applyDiscount</b>: pre —
 * {@code 0 <= discountRate <= 100}; post — result &lt;= {@code total()} when
 * {@code discountRate > 0}.</li>
 * <li><b>PriceCalculator.calculate</b>: pre — {@code basePrice >= 0},
 *       {@code 0 <= discountRate <= 100}, {@code 0 <= taxRate <= 100}; post — result
 * {@code >= 0}.</li>
 * <li><b>ShoppingCart invariant</b>: {@code total() >= 0} after any
 * operation.</li>
 * </ul>
 *
 * <h3>Part B – Write contract tests</h3>
 * Write tests below that:
 * <ol>
 * <li>Verify contracts hold for valid inputs (positive tests).</li>
 * <li>Verify contracts are violated ({@code AssertionError}) for invalid inputs
 * (negative tests).</li>
 * </ol>
 *
 * <p>
 * Use {@code assertThatThrownBy(...).isInstanceOf(AssertionError.class)} to
 * test violations.
 */
class ContractTest {

    private ShoppingCart cart;
    private PriceCalculator calculator;
    private Product product;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
        calculator = new PriceCalculator();
        product = new Product("P001", "Widget", 10.0, 50);
    }

    // -----------------------------------------------------------------------
    // TODO: Write your contract tests below.
    // -----------------------------------------------------------------------
    // --- ShoppingCart.addItem Contracts ---
    @Test
    void addItem_validInput_shouldNotThrow() {
        // Verify pre-condition holds for valid inputs
        assertThatCode(() -> cart.addItem(product, 1)).doesNotThrowAnyException();
    }

    @Test
    void addItem_nullProduct_shouldViolatePreCondition() {
        // Verify AssertionError is thrown for null product
        assertThatThrownBy(() -> cart.addItem(null, 1))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void addItem_invalidQuantity_shouldViolatePreCondition() {
        // Verify AssertionError is thrown for zero or negative quantity
        assertThatThrownBy(() -> cart.addItem(product, 0))
                .isInstanceOf(AssertionError.class);

        assertThatThrownBy(() -> cart.addItem(product, -5))
                .isInstanceOf(AssertionError.class);
    }

    // --- ShoppingCart.applyDiscount Contracts ---
    @Test
    void applyDiscount_validInput_shouldNotThrow() {
        // Verify pre-condition holds for valid discount rate
        cart.addItem(product, 2); // Total: 20.0
        assertThatCode(() -> cart.applyDiscount(10.0)).doesNotThrowAnyException();
    }

    @Test
    void applyDiscount_invalidRate_shouldViolatePreCondition() {
        // Verify AssertionError is thrown for out-of-bounds discount rates
        assertThatThrownBy(() -> cart.applyDiscount(-5.0))
                .isInstanceOf(AssertionError.class);

        assertThatThrownBy(() -> cart.applyDiscount(105.0))
                .isInstanceOf(AssertionError.class);
    }

    // --- PriceCalculator.calculate Contracts ---
    @Test
    void calculate_validInput_shouldNotThrow() {
        // Verify pre-conditions hold for valid calculation parameters
        assertThatCode(() -> calculator.calculate(100.0, 10.0, 18.0)).doesNotThrowAnyException();
    }

    @Test
    void calculate_negativeBasePrice_shouldViolatePreCondition() {
        // Verify AssertionError is thrown for negative base price
        assertThatThrownBy(() -> calculator.calculate(-10.0, 10.0, 18.0))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void calculate_invalidDiscountRate_shouldViolatePreCondition() {
        // Verify AssertionError is thrown for out-of-bounds discount rates
        assertThatThrownBy(() -> calculator.calculate(100.0, -10.0, 18.0))
                .isInstanceOf(AssertionError.class);

        assertThatThrownBy(() -> calculator.calculate(100.0, 110.0, 18.0))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void calculate_invalidTaxRate_shouldViolatePreCondition() {
        // Verify AssertionError is thrown for out-of-bounds tax rates
        assertThatThrownBy(() -> calculator.calculate(100.0, 10.0, -18.0))
                .isInstanceOf(AssertionError.class);

        assertThatThrownBy(() -> calculator.calculate(100.0, 10.0, 118.0))
                .isInstanceOf(AssertionError.class);
    }
    // -----------------------------------------------------------------------
    //
    // EXAMPLE — pre-condition violation (fill in the correct assertion):
    //
    // @Test
    // void addItem_nullProduct_shouldViolatePreCondition() {
    //     assertThatThrownBy(() -> cart.addItem(null, 1))
    //             .isInstanceOf(AssertionError.class);
    // }
    //
    // EXAMPLE — pre-condition holds (valid input):
    //
    // @Test
    // void addItem_validInput_shouldNotThrow() {
    //     assertThatCode(() -> cart.addItem(product, 3)).doesNotThrowAnyException();
    // }
    // -----------------------------------------------------------------------

}
