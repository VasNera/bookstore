package com.neratzis.bookstore.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemInsertDTO(

        @NotNull(message = "{productId.notNull}")
        Long productId,

        @Positive(message = "{quantity.positive}")
        int quantity

) {
}
