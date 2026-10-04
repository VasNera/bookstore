package com.neratzis.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record StationeryInsertDTO(

        @NotBlank(message = "{title.notBlank}")
        String title,

        @NotBlank(message = "{sku.notBlank}")
        String sku,

        @NotNull(message = "{price.notNull}")
        @Positive(message = "{price.positive}")
        BigDecimal price,

        BigDecimal discountPrice,

        String dimensions,

        @PositiveOrZero(message = "{stock.positiveOrZero}")
        int stock,

        String company,

        String imageUrl,

        String description,

        @NotNull(message = "{categoryId.notNull}")
        Long categoryId,

        boolean featured
) {
}
