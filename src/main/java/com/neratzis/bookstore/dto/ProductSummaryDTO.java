package com.neratzis.bookstore.dto;

import com.neratzis.bookstore.model.enums.Availability;

import java.math.BigDecimal;

public record ProductSummaryDTO(

        Long id,

        String title,

        BigDecimal price,

        BigDecimal discountPrice,

        Availability availability,

        String imageUrl
) {
}
