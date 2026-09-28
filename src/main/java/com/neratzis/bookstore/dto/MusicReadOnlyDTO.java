package com.neratzis.bookstore.dto;

import com.neratzis.bookstore.model.enums.Availability;

import java.math.BigDecimal;

public record MusicReadOnlyDTO(

        Long id,

        String title,

        String sku,

        String artist,

        String productionCompany,

        BigDecimal price,

        BigDecimal discountPrice,

        String imageUrl,

        String description,

        String categoryName,

        Availability availability


) {
}
