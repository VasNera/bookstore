package com.neratzis.bookstore.service;

import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.dto.OrderInsertDTO;
import com.neratzis.bookstore.dto.OrderReadOnlyDTO;
import com.neratzis.bookstore.dto.OrderSummaryDTO;
import com.neratzis.bookstore.model.enums.OrderStatus;
import com.neratzis.bookstore.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IOrderService {

OrderReadOnlyDTO createOrder(String username, OrderInsertDTO orderInsertDTO)
        throws EntityNotFoundException, InvalidArgumentException;
Page<OrderSummaryDTO> getMyOrders(String username, Pageable pageable);
OrderReadOnlyDTO getOrderByNumber(String username, String orderNumber)
        throws EntityNotFoundException,InvalidArgumentException;
OrderReadOnlyDTO updateOrderStatus(String orderNumber, OrderStatus orderStatus)
    throws EntityNotFoundException, InvalidArgumentException;
OrderReadOnlyDTO cancelOrder(String username,String orderNumber)
    throws EntityNotFoundException, InvalidArgumentException;
    OrderReadOnlyDTO getOrderByNumberAsAdmin(String orderNumber) throws EntityNotFoundException;
    Page<OrderSummaryDTO> getAllOrders(OrderStatus status, Pageable pageable);
    OrderReadOnlyDTO updatePaymentStatus(String orderNumber, PaymentStatus paymentStatus)
            throws EntityNotFoundException, InvalidArgumentException;

}
