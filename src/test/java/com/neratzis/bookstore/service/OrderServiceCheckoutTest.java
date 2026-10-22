package com.neratzis.bookstore.service;

import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.dto.OrderInsertDTO;
import com.neratzis.bookstore.mapper.OrderMapper;
import com.neratzis.bookstore.model.Cart;
import com.neratzis.bookstore.model.Order;
import com.neratzis.bookstore.model.OrderItem;
import com.neratzis.bookstore.model.Product;
import com.neratzis.bookstore.model.User;
import com.neratzis.bookstore.model.enums.DocumentType;
import com.neratzis.bookstore.model.enums.OrderStatus;
import com.neratzis.bookstore.model.enums.PaymentMethod;
import com.neratzis.bookstore.model.enums.PaymentStatus;
import com.neratzis.bookstore.repository.CartRepository;
import com.neratzis.bookstore.repository.OrderRepository;
import com.neratzis.bookstore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Year;
import java.util.Optional;

import static com.neratzis.bookstore.TestData.book;
import static com.neratzis.bookstore.TestData.cart;
import static com.neratzis.bookstore.TestData.cartItem;
import static com.neratzis.bookstore.TestData.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceCheckoutTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderMapper orderMapper;
    @Spy private ProductPriceCalculator productPriceCalculator = new ProductPriceCalculator();

    @InjectMocks private OrderServiceImpl orderService;

    // ---------- helpers ----------

    private OrderInsertDTO receipt(PaymentMethod method) {
        return new OrderInsertDTO(DocumentType.RECEIPT, method, null, null, null, null, null,
                "Stadiou 10", "Athens", "10564", null);
    }

    private OrderInsertDTO invoice(String business, String taxNumber, String street, String profession) {
        return new OrderInsertDTO(DocumentType.INVOICE, PaymentMethod.CARD, business, taxNumber, street,
                profession, "DOY A Athens", "Stadiou 10", "Athens", "10564", null);
    }

    private void givenUserAndCart(User user, Cart cart) {
        when(userRepository.findByUsernameAndDeletedFalse(user.getUsername())).thenReturn(Optional.of(user));
        when(cartRepository.findByUserUsername(user.getUsername())).thenReturn(Optional.of(cart));
    }

    private void givenMapperAndRepository() {
        when(orderMapper.mapToOrderEntity(any(), any())).thenAnswer(invocation -> {
            Order order = new Order();
            order.setUser(invocation.getArgument(1));
            return order;
        });
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(7L); // what the database would do
            return order;
        });
    }

    private Order savedOrder() {
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).saveAndFlush(captor.capture());
        return captor.getValue();
    }

    // ---------- the happy path ----------

    @Test
    void checkout_decreasesStock_snapshotsPrices_calculatesTotal_andEmptiesTheCart() throws Exception {
        Product discounted = book(1, "14.90", "11.90", 10);
        Product regular = book(2, "20.00", null, 4);
        User user = user("maria");
        Cart cart = cart(user);
        cart.addCartItem(cartItem(1, discounted, 2));
        cart.addCartItem(cartItem(2, regular, 1));
        givenUserAndCart(user, cart);
        givenMapperAndRepository();

        orderService.createOrder("maria", receipt(PaymentMethod.CARD));

        Order order = savedOrder();
        assertThat(discounted.getStock()).isEqualTo(8);
        assertThat(regular.getStock()).isEqualTo(3);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("43.80"); // 2 x 11.90 + 20.00
        assertThat(order.getOrderItems()).hasSize(2);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getOrderNumber()).isEqualTo("ORD-%d-000007".formatted(Year.now().getValue()));
        assertThat(cart.getCartItems()).isEmpty();

        OrderItem line = order.getOrderItems().stream()
                .filter(item -> item.getProduct() == discounted)
                .findFirst().orElseThrow();
        assertThat(line.getProductTitle()).isEqualTo("Book 1");
        assertThat(line.getPriceAtPurchase()).isEqualByComparingTo("11.90"); // snapshot of the discount price
        assertThat(line.getQuantity()).isEqualTo(2);
    }

    @Test
    void checkout_allowsBuyingExactlyTheRemainingStock() throws Exception {
        Product product = book(1, "10.00", null, 3);
        User user = user("maria");
        Cart cart = cart(user);
        cart.addCartItem(cartItem(1, product, 3));
        givenUserAndCart(user, cart);
        givenMapperAndRepository();

        orderService.createOrder("maria", receipt(PaymentMethod.CARD));

        assertThat(product.getStock()).isZero();
    }

    @ParameterizedTest
    @EnumSource(PaymentMethod.class)
    void checkout_startsEveryOrderWithPaymentStatusPending(PaymentMethod method) throws Exception {
        User user = user("maria");
        Cart cart = cart(user);
        cart.addCartItem(cartItem(1, book(1, "10.00", null, 5), 1));
        givenUserAndCart(user, cart);
        givenMapperAndRepository();

        orderService.createOrder("maria", receipt(method));

        assertThat(savedOrder().getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    // ---------- invoice vs receipt ----------

    @Test
    void checkout_withInvoiceAndCompleteBillingData_succeeds() throws Exception {
        User user = user("maria");
        Cart cart = cart(user);
        cart.addCartItem(cartItem(1, book(1, "10.00", null, 5), 1));
        givenUserAndCart(user, cart);
        givenMapperAndRepository();

        orderService.createOrder("maria", invoice("Test LTD", "123456789", "Ermou 5", "Retail"));

        verify(orderRepository).saveAndFlush(any(Order.class));
    }

    @Test
    void checkout_withInvoiceAndMissingBillingData_isRejected_andNothingIsSaved() throws Exception {
        User user = user("maria");
        Cart cart = cart(user);
        Product product = book(1, "10.00", null, 5);
        cart.addCartItem(cartItem(1, product, 1));
        givenUserAndCart(user, cart);
        givenMapperAndRepository();

        assertThatThrownBy(() -> orderService.createOrder("maria", invoice(null, "123456789", "Ermou 5", "Retail")))
                .isInstanceOf(InvalidArgumentException.class);
        assertThatThrownBy(() -> orderService.createOrder("maria", invoice("Test LTD", " ", "Ermou 5", "Retail")))
                .isInstanceOf(InvalidArgumentException.class);
        assertThatThrownBy(() -> orderService.createOrder("maria", invoice("Test LTD", "123456789", null, "Retail")))
                .isInstanceOf(InvalidArgumentException.class);
        assertThatThrownBy(() -> orderService.createOrder("maria", invoice("Test LTD", "123456789", "Ermou 5", "")))
                .isInstanceOf(InvalidArgumentException.class);

        verify(orderRepository, never()).saveAndFlush(any(Order.class));
        assertThat(product.getStock()).isEqualTo(5); // untouched
    }

    // ---------- failures ----------

    @Test
    void checkout_withAnEmptyCart_isRejected() {
        User user = user("maria");
        givenUserAndCart(user, cart(user));
        givenMapperAndRepository();

        assertThatThrownBy(() -> orderService.createOrder("maria", receipt(PaymentMethod.CARD)))
                .isInstanceOf(InvalidArgumentException.class);

        verify(orderRepository, never()).saveAndFlush(any(Order.class));
    }

    @Test
    void checkout_withNotEnoughStock_isRejected_andStockIsNotChanged() throws Exception {
        Product product = book(1, "10.00", null, 2);
        User user = user("maria");
        Cart cart = cart(user);
        cart.addCartItem(cartItem(1, product, 3));
        givenUserAndCart(user, cart);
        givenMapperAndRepository();

        assertThatThrownBy(() -> orderService.createOrder("maria", receipt(PaymentMethod.CARD)))
                .isInstanceOf(InvalidArgumentException.class);

        assertThat(product.getStock()).isEqualTo(2);
        verify(orderRepository, never()).saveAndFlush(any(Order.class));
    }

    @Test
    void checkout_withADeletedProductInTheCart_isRejected() {
        Product product = book(1, "10.00", null, 5);
        product.softDelete();
        User user = user("maria");
        Cart cart = cart(user);
        cart.addCartItem(cartItem(1, product, 1));
        givenUserAndCart(user, cart);
        givenMapperAndRepository();

        assertThatThrownBy(() -> orderService.createOrder("maria", receipt(PaymentMethod.CARD)))
                .isInstanceOf(InvalidArgumentException.class);

        verify(orderRepository, never()).saveAndFlush(any(Order.class));
    }

    @Test
    void checkout_forAnUnknownUser_throwsNotFound() {
        when(userRepository.findByUsernameAndDeletedFalse("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder("ghost", receipt(PaymentMethod.CARD)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void checkout_ignoresAnInvalidDiscount_andChargesTheNormalPrice() throws Exception {
        Product product = book(1, "10.00", "15.00", 5); // "discount" higher than the price
        User user = user("maria");
        Cart cart = cart(user);
        cart.addCartItem(cartItem(1, product, 2));
        givenUserAndCart(user, cart);
        givenMapperAndRepository();

        orderService.createOrder("maria", receipt(PaymentMethod.CARD));

        assertThat(savedOrder().getTotalAmount()).isEqualByComparingTo("20.00");
    }
}
