package com.neratzis.bookstore.service;


import com.neratzis.bookstore.core.exceptions.AppGenericException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.dto.OrderInsertDTO;
import com.neratzis.bookstore.dto.OrderReadOnlyDTO;
import com.neratzis.bookstore.dto.OrderSummaryDTO;
import com.neratzis.bookstore.mapper.OrderMapper;
import com.neratzis.bookstore.model.*;
import com.neratzis.bookstore.model.enums.DocumentType;
import com.neratzis.bookstore.model.enums.OrderStatus;
import com.neratzis.bookstore.model.enums.PaymentMethod;
import com.neratzis.bookstore.model.enums.PaymentStatus;
import com.neratzis.bookstore.repository.CartRepository;
import com.neratzis.bookstore.repository.OrderRepository;
import com.neratzis.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements IOrderService{

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductPriceCalculator productPriceCalculator;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public OrderReadOnlyDTO createOrder(String username, OrderInsertDTO orderInsertDTO)
            throws EntityNotFoundException, InvalidArgumentException {

        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(()-> new EntityNotFoundException("USER", "User with username: " + username +
                        " not found"));
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(()-> new EntityNotFoundException("CART", "Cart for user: " + username +
                "not found"));

        if (cart.getCartItems().isEmpty()){
            throw new InvalidArgumentException("CART", "Cart is empty");
        }
        validateInvoiceData(orderInsertDTO);

        Order order = orderMapper.mapToOrderEntity(orderInsertDTO,user);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(resolveInitialPaymentStatus(orderInsertDTO.paymentMethod()));
        order.setOrderNumber("TEMP-" + UUID.randomUUID());

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getCartItems()){
            Product product = cartItem.getProduct();

            if (product.isDeleted()){
                throw new InvalidArgumentException("PRODUCT", "Product is not available");
            }
            if (product.getStock() < cartItem.getQuantity()){
                throw new InvalidArgumentException("ORDER", "No stock for product: " + product.getTitle() +
                        "Stock is: " + product.getStock());
            }
            product.setStock(product.getStock() - cartItem.getQuantity());

            BigDecimal unitPrice = productPriceCalculator.effectivePrice(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setProductTitle(product.getTitle());
            orderItem.setPriceAtPurchase(unitPrice);
            orderItem.setQuantity(cartItem.getQuantity());
            order.addOrderItem(orderItem);

            total = total.add(unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.saveAndFlush(order);
        saved.setOrderNumber("ORD-%d-%06d".formatted(Year.now().getValue(), saved.getId()));
        cart.getCartItems().clear();

        log.info("Created order ={} for user ={}. The amount is ={}", saved.getOrderNumber(), username, total);
        return orderMapper.toReadOnlyDTO(saved);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryDTO> getMyOrders(String username, Pageable pageable) {
        return orderRepository.findByUserUsernameOrderByCreatedAtDesc(username,pageable)
                .map(orderMapper::toSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderReadOnlyDTO getOrderByNumber(String username, String orderNumber)
            throws EntityNotFoundException, InvalidArgumentException {

        Order order = findOrder(orderNumber);
        if (!order.getUser().getUsername().equals(username)) {
        throw new InvalidArgumentException("ORDER", "This order is not yours");
        }
        return orderMapper.toReadOnlyDTO(order);
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public OrderReadOnlyDTO updateOrderStatus(String orderNumber, OrderStatus newStatus)
            throws EntityNotFoundException, InvalidArgumentException {

        Order order = findOrder(orderNumber);
        validateTransition(order.getOrderStatus(), newStatus);

        order.setOrderStatus(newStatus);

        if (newStatus == OrderStatus.DELIVERED
                && order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY){
            order.setPaymentStatus(PaymentStatus.PAID);
        }
        log.info("Order ={} status changed to ={}" ,orderNumber, newStatus);
        return orderMapper.toReadOnlyDTO(order);
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public OrderReadOnlyDTO cancelOrder(String username, String orderNumber)
            throws EntityNotFoundException, InvalidArgumentException {
        Order order = findOrder(orderNumber);

        if (!order.getUser().getUsername().equals(username)){
            throw new InvalidArgumentException("ORDER", "This order is not yours");
        }

        if (order.getOrderStatus() == OrderStatus.SHIPPED
                || order.getOrderStatus() == OrderStatus.DELIVERED){
            throw new InvalidArgumentException("ORDER", "Order cannot cancelled because is on the road");
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED){
            throw new InvalidArgumentException("ORDER", "Order is already cancelled");
        }
        order.getOrderItems().forEach(
                orderItem -> orderItem.getProduct().setStock(orderItem.getProduct()
                        .getStock() + orderItem.getQuantity()));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        log.info("Order={} cancelled by user={}, stock restored", orderNumber, username);
        return orderMapper.toReadOnlyDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderReadOnlyDTO getOrderByNumberAsAdmin(String orderNumber) throws EntityNotFoundException {
        return orderMapper.toReadOnlyDTO(findOrder(orderNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryDTO> getAllOrders(OrderStatus status, Pageable pageable) {
        Page<Order> orders = (status == null)
                ? orderRepository.findAllByOrderByCreatedAtDesc(pageable)
                : orderRepository.findByOrderStatusOrderByCreatedAtDesc(status, pageable);
        return orders.map(orderMapper::toSummaryDTO);
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public OrderReadOnlyDTO updatePaymentStatus(String orderNumber, PaymentStatus newStatus)
            throws EntityNotFoundException, InvalidArgumentException {

        Order order = findOrder(orderNumber);

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new InvalidArgumentException("ORDER", "Cannot change payment of a cancelled order");
        }
        validatePaymentTransition(order.getPaymentStatus(), newStatus);

        order.setPaymentStatus(newStatus);
        log.info("Order ={} payment status changed to ={}", orderNumber, newStatus);
        return orderMapper.toReadOnlyDTO(order);
    }

    //Helpers

    private Order findOrder(String orderNumber) throws EntityNotFoundException{
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(()-> new EntityNotFoundException("ORDER","Order with number: "
                + orderNumber + " not found" ));
    }

    private void validateInvoiceData(OrderInsertDTO orderInsertDTO)
            throws EntityNotFoundException , InvalidArgumentException{
        if (orderInsertDTO.documentType() != DocumentType.INVOICE) return;
        if (isBlank(orderInsertDTO.billingBusinessName()) || isBlank(orderInsertDTO.billingProfession())
         || isBlank(orderInsertDTO.billingTaxNumber()) || isBlank(orderInsertDTO.billingStreet())) {
            throw new InvalidArgumentException("ORDER", "For invoice document you need Business name, profession," +
                    "tax number and billing street");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private PaymentStatus resolveInitialPaymentStatus(PaymentMethod paymentMethod) {
        return switch (paymentMethod){
            case CARD , PAYPAL-> PaymentStatus.PENDING;
            case CASH_ON_DELIVERY, BANK_TRANSFER -> PaymentStatus.PENDING;
        };
    }

    private void validateTransition(OrderStatus current, OrderStatus next) throws InvalidArgumentException{
        boolean allowed = switch (current){
            case PENDING -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
        if (!allowed){ throw new InvalidArgumentException("ORDER", "Order can't deliver");
        }
    }

    private void validatePaymentTransition(PaymentStatus current, PaymentStatus next)
            throws InvalidArgumentException {
        boolean allowed = switch (current) {
            case PENDING -> next == PaymentStatus.PAID || next == PaymentStatus.FAILED;
            case PAID -> next == PaymentStatus.REFUNDED;
            case FAILED, REFUNDED -> false;
        };
        if (!allowed) {
            throw new InvalidArgumentException("ORDER",
                    "Payment status cannot change from " + current + " to " + next);
        }
    }
}
