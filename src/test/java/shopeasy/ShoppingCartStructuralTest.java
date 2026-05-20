package shopeasy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Task 2 – Structural Testing &amp; Code Coverage (Chapter 3)
 *
 * <p>
 * Target class: {@link ShoppingCart}
 *
 * <h3>Workflow</h3>
 * <ol>
 * <li>Write an initial test suite based on the specification (Javadoc of
 * ShoppingCart).</li>
 * <li>Run {@code mvn test} to generate the JaCoCo report:
 * <pre>  target/site/jacoco/index.html</pre></li>
 * <li>Open the report, navigate to {@code ShoppingCart}, and identify uncovered
 * branches.</li>
 * <li>Add tests specifically to cover those branches until branch coverage
 * &gt;= 80%.</li>
 * <li>Take a screenshot of the final JaCoCo summary and put it in
 * {@code report/jacoco-screenshot.png}.</li>
 * </ol>
 *
 * <h3>Branches to think about</h3>
 * <ul>
 * <li>{@code addItem}: product already in cart vs. new product</li>
 * <li>{@code removeItem}: product found vs. not found in cart</li>
 * <li>{@code updateQuantity}: product found vs. not found, quantity valid vs.
 * invalid</li>
 * <li>{@code applyDiscount}: zero discount, positive discount</li>
 * <li>{@code total}: empty cart vs. non-empty cart</li>
 * </ul>
 *
 * <h3>Bonus (PIT Mutation Testing)</h3>
 * Run: {@code mvn org.pitest:pitest-maven:mutationCoverage}
 * <br>Examine the HTML report in {@code target/pit-reports/}. Find two
 * surviving mutants, explain why each survived, and describe a test that would
 * kill it. Add this analysis to your reflection report.
 */
class ShoppingCartStructuralTest {

    private ShoppingCart cart;
    private Product apple;
    private Product banana;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
        apple = new Product("P001", "Apple", 1.50, 100);
        banana = new Product("P002", "Banana", 0.80, 50);
    }

    // -----------------------------------------------------------------------
    // TODO: Write your tests below.
    // -----------------------------------------------------------------------
    @Test
    void addItem_newProduct_shouldAddNewCartItem() {
        // Branch: Product not found in the loop, so a new item is added.
        cart.addItem(apple, 2);
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isEqualTo(3.0); // 2 * 1.50
    }

    @Test
    void addItem_existingProduct_shouldCombineQuantities() {
        // Branch: Product found in the loop (ID matches), quantity is updated.
        cart.addItem(apple, 2);
        cart.addItem(apple, 3); // Adding the same product again

        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(cart.total()).isEqualTo(7.50); // 5 * 1.50
    }

    @Test
    void removeItem_existingProduct_shouldRemoveFromCart() {
        // Branch: removeIf lambda evaluates to true
        cart.addItem(apple, 1);
        cart.addItem(banana, 2);

        cart.removeItem(apple.getId()); // Remove Apple

        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.getItems().get(0).getProduct().getId()).isEqualTo(banana.getId());
    }

    @Test
    void removeItem_nonExistingProduct_shouldDoNothing() {
        // Branch: removeIf lambda evaluates to false
        cart.addItem(apple, 1);

        cart.removeItem("NON_EXISTING_ID");

        assertThat(cart.itemCount()).isEqualTo(1);
    }

    @Test
    void updateQuantity_validQuantityAndProduct_shouldUpdate() {
        // Branch: quantity <= 0 is false, product is found (true)
        cart.addItem(banana, 1);
        cart.updateQuantity(banana.getId(), 5);

        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(cart.total()).isEqualTo(4.0); // 5 * 0.80
    }

    @Test
    void updateQuantity_invalidQuantity_shouldThrowException() {
        // Branch: quantity <= 0 is true
        cart.addItem(apple, 1);

        assertThatThrownBy(() -> cart.updateQuantity(apple.getId(), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be > 0");
    }

    @Test
    void updateQuantity_productNotFound_shouldThrowException() {
        // Branch: quantity <= 0 is false, loop finishes without finding the product (false)
        cart.addItem(apple, 1);

        assertThatThrownBy(() -> cart.updateQuantity("NON_EXISTING_ID", 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found in cart");
    }

    @Test
    void applyDiscount_shouldCalculateCorrectly() {
        // Branch: applyDiscount logic execution
        cart.addItem(apple, 2);  // 3.0
        cart.addItem(banana, 5); // 4.0
        // Total: 7.0

        double discountedTotal = cart.applyDiscount(10.0); // 10% discount (0.7) -> 6.30

        assertThat(discountedTotal).isEqualTo(6.30);
    }

    @Test
    void total_emptyCart_shouldReturnZero() {
        // Branch: items loop does not execute because the cart is empty
        assertThat(cart.total()).isEqualTo(0.0);
    }

    @Test
    void utilityMethods_shouldWorkCorrectly() {
        // Covers line coverage for getItems, clear, and toString methods
        cart.addItem(apple, 2);
        assertThat(cart.getItems()).hasSize(1);

        String toStringResult = cart.toString();

        // Dynamically formatting the expected string to handle OS locale differences (comma vs dot)
        String expectedTotalStr = String.format("%.2f", 3.0);

        assertThat(toStringResult)
                .contains("items=1")
                .contains("total=" + expectedTotalStr);

        cart.clear();
        assertThat(cart.itemCount()).isEqualTo(0);
        assertThat(cart.total()).isEqualTo(0.0);
    }

    // -----------------------------------------------------------------------
    //
    // Start with happy-path tests, then add tests that target specific branches.
    //
    // HINT: Run `mvn test` after every few tests to see coverage progress.
    // -----------------------------------------------------------------------
}
