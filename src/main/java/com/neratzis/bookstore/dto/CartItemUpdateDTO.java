package com.neratzis.bookstore.dto;

import jakarta.validation.constraints.Positive;

public record CartItemUpdateDTO(
        @Positive(message = "{quantity.positive}")
        int quantity
) {
}
