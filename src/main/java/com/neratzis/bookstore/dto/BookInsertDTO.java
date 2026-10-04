package com.neratzis.bookstore.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record BookInsertDTO(

        @NotBlank(message = "{title.notBlank}")
        String title,

        @NotBlank(message = "{sku.notBlank}")
        String sku,

        @NotBlank(message = "{isbn.notBlank}")
        String isbn,

        String dimensions,

        @NotNull(message = "{pages.notNull}")
        Integer pages,

        @NotNull(message = "{price.notNull}")
        @Positive
        BigDecimal price,

        BigDecimal discountPrice,

        @PositiveOrZero(message = "{stock.positiveOrZero}")
        int stock,

        String author,

        String publisher,

        Integer releaseYear,

        String imageUrl,

        String description,

        @NotNull(message = "{categoryId.notNull}")
        Long categoryId,

        boolean featured

) {
}
