package com.neratzis.bookstore.service;

import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.mapper.OrderMapper;
import com.neratzis.bookstore.model.Order;
import com.neratzis.bookstore.model.Product;
import com.neratzis.bookstore.model.User;
import com.neratzis.bookstore.model.enums.OrderStatus;
import com.neratzis.bookstore.model.enums.PaymentStatus;
import com.neratzis.bookstore.repository.CartRepository;
import com.neratzis.bookstore.repository.OrderRepository;
import com.neratzis.bookstore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static com.neratzis.bookstore.TestData.book;
import static com.neratzis.bookstore.TestData.order;
import static com.neratzis.bookstore.TestData.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceCancelAndAccessTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderMapper orderMapper;
    @Spy private ProductPriceCalculator productPriceCalculator = new ProductPriceCalculator();

    @InjectMocks private OrderServiceImpl orderService;

    private final User owner = user("maria");
    private final User other = user("giorgos");

    private Order givenOrder(OrderStatus status, PaymentStatus paymentStatus, Product product, int quantity) {
        Order order = order(owner, status, paymentStatus, product, quantity);
        when(orderRepository.findByOrderNumber("ORD-1")).thenReturn(Optional.of(order));
        return order;
    }

    // ---------- cancel ----------

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PENDING", "CONFIRMED"})
    void cancel_restoresTheStock_andMarksTheOrderCancelled(OrderStatus status) throws Exception {
        Product product = book(1, "10.00", null, 3);
        Order order = givenOrder(status, PaymentStatus.PENDING, product, 2);

        orderService.cancelOrder("maria", "ORD-1");

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(product.getStock()).isEqualTo(5); // 3 + the 2 that were bought
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING); // nothing was paid, nothing to refund
    }

    @Test
    void cancel_aPaidOrder_marksThePaymentAsRefunded() throws Exception {
        Order order = givenOrder(OrderStatus.CONFIRMED, PaymentStatus.PAID, book(1, "10.00", null, 3), 1);

        orderService.cancelOrder("maria", "ORD-1");

        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void cancel_twice_isRejected_andTheStockIsRestoredOnlyOnce() throws Exception {
        Product product = book(1, "10.00", null, 3);
        givenOrder(OrderStatus.PENDING, PaymentStatus.PENDING, product, 2);

        orderService.cancelOrder("maria", "ORD-1");
        assertThatThrownBy(() -> orderService.cancelOrder("maria", "ORD-1"))
                .isInstanceOf(InvalidArgumentException.class);

        assertThat(product.getStock()).isEqualTo(5);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"SHIPPED", "DELIVERED"})
    void cancel_anOrderThatIsAlreadyOnTheRoad_isRejected(OrderStatus status) {
        Product product = book(1, "10.00", null, 3);
        Order order = givenOrder(status, PaymentStatus.PAID, product, 2);

        assertThatThrownBy(() -> orderService.cancelOrder("maria", "ORD-1"))
                .isInstanceOf(InvalidArgumentException.class);

        assertThat(order.getOrderStatus()).isEqualTo(status);
        assertThat(product.getStock()).isEqualTo(3);
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void cancel_someoneElsesOrder_isRejected_andNothingChanges() {
        Product product = book(1, "10.00", null, 3);
        Order order = givenOrder(OrderStatus.PENDING, PaymentStatus.PENDING, product, 2);

        assertThatThrownBy(() -> orderService.cancelOrder(other.getUsername(), "ORD-1"))
                .isInstanceOf(InvalidArgumentException.class);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(product.getStock()).isEqualTo(3);
    }

    @Test
    void cancel_anOrderThatDoesNotExist_throwsNotFound() {
        when(orderRepository.findByOrderNumber("ORD-404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder("maria", "ORD-404"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ---------- who can read an order ----------

    @Test
    void theOwner_canReadTheirOrder() throws Exception {
        Order order = givenOrder(OrderStatus.PENDING, PaymentStatus.PENDING, book(1, "10.00", null, 3), 1);

        orderService.getOrderByNumber("maria", "ORD-1");

        verify(orderMapper).toReadOnlyDTO(order);
    }

    @Test
    void anotherCustomer_cannotReadSomeoneElsesOrder() {
        givenOrder(OrderStatus.PENDING, PaymentStatus.PENDING, book(1, "10.00", null, 3), 1);

        assertThatThrownBy(() -> orderService.getOrderByNumber("giorgos", "ORD-1"))
                .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    void anAdmin_canReadAnyOrder_withoutAnOwnershipCheck() throws Exception {
        Order order = givenOrder(OrderStatus.PENDING, PaymentStatus.PENDING, book(1, "10.00", null, 3), 1);

        orderService.getOrderByNumberAsAdmin("ORD-1");

        verify(orderMapper).toReadOnlyDTO(order);
    }

    @Test
    void readingAnOrderThatDoesNotExist_throwsNotFound() {
        when(orderRepository.findByOrderNumber("ORD-404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderByNumber("maria", "ORD-404"))
                .isInstanceOf(EntityNotFoundException.class);
        assertThatThrownBy(() -> orderService.getOrderByNumberAsAdmin("ORD-404"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
