package com.neratzis.bookstore.dto;

import java.math.BigDecimal;

public record ToyInsertDTO(

        String title,

        String sku,

        BigDecimal price,

        String dimensions,

        BigDecimal discountPrice,

        int stock,

        String ageRange,

        String imageUrl,

        String description,

        Long categoryId,

        boolean featured
) {
}
