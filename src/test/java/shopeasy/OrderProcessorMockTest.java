package shopeasy;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Task 5 – Mocks &amp; Stubs (Chapter 6)
 *
 * <p>
 * Target class: {@link OrderProcessor}
 *
 * <p>
 * Use Mockito to mock {@link InventoryService} and {@link PaymentGateway}, then
 * test {@link OrderProcessor#process(String, ShoppingCart)} in isolation.
 *
 * <h3>Required scenarios (at least 4)</h3>
 * <ol>
 * <li><b>Happy path</b> — inventory available, payment succeeds → non-null
 * {@link Order} returned.</li>
 * <li><b>Inventory failure</b> — {@code isAvailable()} returns {@code false}
 * for at least one item → method returns {@code null} AND {@code charge()} is
 * <em>never</em> called.</li>
 * <li><b>Payment failure</b> — inventory OK, {@code charge()} returns
 * {@code false} → method returns {@code null}.</li>
 * <li><b>Partial quantity</b> — define the expected behaviour when only some
 * items pass the inventory check, and write a test for it.</li>
 * </ol>
 *
 * <h3>Verification</h3>
 * Use {@code verify(paymentGateway, never()).charge(...)} to assert that
 * payment is never attempted when inventory is insufficient.
 *
 * <h3>Reflection (add to your report)</h3>
 * Answer: What does mocking allow you to test that you could not test
 * otherwise? What does it prevent you from testing? When is mocking a bad idea?
 */
@ExtendWith(MockitoExtension.class)
class OrderProcessorMockTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private OrderProcessor orderProcessor;

    private ShoppingCart cart;
    private Product widget;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
        widget = new Product("P001", "Widget", 25.0, 100);
    }

    // -----------------------------------------------------------------------
    // TODO: Write your mock-based tests below.
    // -----------------------------------------------------------------------
    /**
     * Scenario 1: Happy path Inventory is available and payment succeeds.
     * Method returns a non-null Order object.
     */
    @Test
    void process_inventoryOkAndPaymentOk_returnsOrder() {
        cart.addItem(widget, 2);

        when(inventoryService.isAvailable(widget, 2)).thenReturn(true);
        when(paymentGateway.charge("customer-1", 50.0)).thenReturn(true);

        Order order = orderProcessor.process("customer-1", cart);

        assertThat(order).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo("customer-1");
        assertThat(order.getTotal()).isEqualTo(50.0);

        verify(inventoryService).isAvailable(widget, 2);
        verify(paymentGateway).charge("customer-1", 50.0);
    }

    /**
     * Scenario 2: Inventory failure isAvailable() returns false for at least
     * one item. The method must return null AND charge() must never be called.
     */
    @Test
    void process_inventoryFails_returnsNullAndDoesNotCharge() {
        cart.addItem(widget, 5);

        // Mock inventory to fail
        when(inventoryService.isAvailable(widget, 5)).thenReturn(false);

        Order order = orderProcessor.process("customer-1", cart);

        assertThat(order).isNull();

        // Critical Verification: Ensure paymentGateway was NEVER called
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
    }

    /**
     * Scenario 3: Payment failure Inventory is OK, but charge() returns false.
     * The method must return null indicating the order was not created.
     */
    @Test
    void process_inventoryOkButPaymentFails_returnsNull() {
        cart.addItem(widget, 1);

        when(inventoryService.isAvailable(widget, 1)).thenReturn(true);
        when(paymentGateway.charge("customer-1", 25.0)).thenReturn(false);

        Order order = orderProcessor.process("customer-1", cart);

        assertThat(order).isNull();

        // Verification: Ensure payment was attempted
        verify(paymentGateway).charge("customer-1", 25.0);
    }

    /**
     * Scenario 4: Partial quantity (Multiple items in cart, one fails) Expected
     * behavior: If the cart has multiple distinct items and one of them fails
     * the inventory check, the entire process aborts immediately. Payment is
     * never attempted for partial orders.
     */
    @Test
    void process_partialQuantityInCart_abortsProcessAndDoesNotCharge() {
        Product anotherWidget = new Product("P002", "Premium Widget", 100.0, 50);

        cart.addItem(widget, 2);         // Item 1
        cart.addItem(anotherWidget, 1);  // Item 2

        // The first item is available, but the second one is out of stock
        when(inventoryService.isAvailable(widget, 2)).thenReturn(true);
        when(inventoryService.isAvailable(anotherWidget, 1)).thenReturn(false);

        Order order = orderProcessor.process("customer-1", cart);

        assertThat(order).isNull();

        // Verification: Ensure partial payment is never charged
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
    }

    // -----------------------------------------------------------------------
    //
    // EXAMPLE STRUCTURE — happy path:
    //
    // @Test
    // void process_inventoryOkAndPaymentOk_returnsOrder() {
    //     cart.addItem(widget, 2);
    //
    //     when(inventoryService.isAvailable(widget, 2)).thenReturn(true);
    //     when(paymentGateway.charge("customer-1", 50.0)).thenReturn(true);
    //
    //     Order order = orderProcessor.process("customer-1", cart);
    //
    //     assertThat(order).isNotNull();
    //     assertThat(order.getCustomerId()).isEqualTo("customer-1");
    //     assertThat(order.getTotal()).isEqualTo(50.0);
    //     verify(paymentGateway).charge("customer-1", 50.0);
    // }
    // -----------------------------------------------------------------------
}
