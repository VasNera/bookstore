package com.neratzis.bookstore.dto;

import com.neratzis.bookstore.model.enums.OrderStatus;
import com.neratzis.bookstore.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryDTO(

        String orderNumber,

        Instant createdAt,

        OrderStatus orderStatus,

        PaymentStatus paymentStatus,


        BigDecimal totalAmount

) {
}
