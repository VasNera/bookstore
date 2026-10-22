package com.neratzis.bookstore.service;

import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.mapper.OrderMapper;
import com.neratzis.bookstore.model.Order;
import com.neratzis.bookstore.model.enums.OrderStatus;
import com.neratzis.bookstore.model.enums.PaymentMethod;
import com.neratzis.bookstore.model.enums.PaymentStatus;
import com.neratzis.bookstore.repository.CartRepository;
import com.neratzis.bookstore.repository.OrderRepository;
import com.neratzis.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductPriceCalculator productPriceCalculator;
    @Mock private OrderMapper orderMapper;

    @InjectMocks private OrderServiceImpl orderService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPaymentMethod(PaymentMethod.CARD);
        when(orderRepository.findByOrderNumber("ORD-1")).thenReturn(Optional.of(order));
    }

    // ---------- order status ----------

    @ParameterizedTest(name = "{0} -> {1} is allowed")
    @CsvSource({
            "PENDING, CONFIRMED",
            "PENDING, CANCELLED",
            "CONFIRMED, SHIPPED",
            "CONFIRMED, CANCELLED",
            "SHIPPED, DELIVERED"
    })
    void updateOrderStatus_allowsValidTransitions(OrderStatus from, OrderStatus to) throws Exception {
        order.setOrderStatus(from);

        orderService.updateOrderStatus("ORD-1", to);

        assertThat(order.getOrderStatus()).isEqualTo(to);
    }

    @ParameterizedTest(name = "{0} -> {1} is rejected")
    @CsvSource({
            "PENDING, SHIPPED",
            "PENDING, DELIVERED",
            "CONFIRMED, PENDING",
            "SHIPPED, CANCELLED",
            "SHIPPED, PENDING",
            "DELIVERED, PENDING",
            "DELIVERED, CANCELLED",
            "CANCELLED, CONFIRMED",
            "CANCELLED, PENDING"
    })
    void updateOrderStatus_rejectsInvalidTransitions(OrderStatus from, OrderStatus to) {
        order.setOrderStatus(from);

        assertThatThrownBy(() -> orderService.updateOrderStatus("ORD-1", to))
                .isInstanceOf(InvalidArgumentException.class);

        assertThat(order.getOrderStatus()).isEqualTo(from); // unchanged
    }

    @Test
    void delivering_aCashOnDeliveryOrder_marksItAsPaid() throws Exception {
        order.setOrderStatus(OrderStatus.SHIPPED);
        order.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        orderService.updateOrderStatus("ORD-1", OrderStatus.DELIVERED);

        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void delivering_aCardOrder_doesNotChangePaymentStatus() throws Exception {
        order.setOrderStatus(OrderStatus.SHIPPED);
        order.setPaymentMethod(PaymentMethod.CARD);

        orderService.updateOrderStatus("ORD-1", OrderStatus.DELIVERED);

        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    // ---------- payment status ----------

    @ParameterizedTest(name = "payment {0} -> {1} is allowed")
    @CsvSource({
            "PENDING, PAID",
            "PENDING, FAILED",
            "PAID, REFUNDED"
    })
    void updatePaymentStatus_allowsValidTransitions(PaymentStatus from, PaymentStatus to) throws Exception {
        order.setPaymentStatus(from);

        orderService.updatePaymentStatus("ORD-1", to);

        assertThat(order.getPaymentStatus()).isEqualTo(to);
    }

    @ParameterizedTest(name = "payment {0} -> {1} is rejected")
    @CsvSource({
            "PENDING, REFUNDED",
            "PAID, PAID",
            "PAID, FAILED",
            "PAID, PENDING",
            "FAILED, PAID",
            "REFUNDED, PAID"
    })
    void updatePaymentStatus_rejectsInvalidTransitions(PaymentStatus from, PaymentStatus to) {
        order.setPaymentStatus(from);

        assertThatThrownBy(() -> orderService.updatePaymentStatus("ORD-1", to))
                .isInstanceOf(InvalidArgumentException.class);

        assertThat(order.getPaymentStatus()).isEqualTo(from);
    }

    @Test
    void updatePaymentStatus_isRejected_forCancelledOrders() {
        order.setOrderStatus(OrderStatus.CANCELLED);

        assertThatThrownBy(() -> orderService.updatePaymentStatus("ORD-1", PaymentStatus.PAID))
                .isInstanceOf(InvalidArgumentException.class);
    }
}