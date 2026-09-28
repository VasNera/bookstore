package com.neratzis.bookstore.dto;

import java.math.BigDecimal;

public record StationeryInsertDTO(

        String title,

        String sku,

        BigDecimal price,

        BigDecimal discountPrice,

        String dimensions,

        int stock,

        String company,

        String imageUrl,

        String description,

        Long categoryId,

        boolean featured
) {
}
