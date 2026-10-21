package com.neratzis.bookstore.dto;

import com.neratzis.bookstore.model.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record OrderPaymentStatusUpdateDTO(

        @NotNull(message = "{paymentStatus.notNull}")
        PaymentStatus paymentStatus
) {
}
