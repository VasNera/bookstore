package com.neratzis.bookstore.dto;

import com.neratzis.bookstore.model.enums.DocumentType;
import com.neratzis.bookstore.model.enums.OrderStatus;
import com.neratzis.bookstore.model.enums.PaymentMethod;
import com.neratzis.bookstore.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderReadOnlyDTO(


        String orderNumber,

        Instant createdAt,

        DocumentType documentType,

        OrderStatus orderStatus,

        PaymentMethod paymentMethod,

        PaymentStatus paymentStatus,

        String billingBusinessName,

        String billingTaxNumber,

        String billingStreet,

        String billingProfession,

        String billingTaxOffice,

        String shippingStreet,

        String shippingCity,

        String shippingPostalCode,

        String notes,

        BigDecimal totalAmount,

        List<OrderItemReadOnlyDTO> items


) {
}
