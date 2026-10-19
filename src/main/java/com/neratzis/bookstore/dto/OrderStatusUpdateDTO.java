package com.neratzis.bookstore.dto;

import com.neratzis.bookstore.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateDTO(

        @NotNull(message = "{orderStatus.notNull}")
        OrderStatus orderStatus
) {
}
