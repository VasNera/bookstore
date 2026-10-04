package com.neratzis.bookstore.dto;

import com.neratzis.bookstore.model.enums.Availability;

import java.math.BigDecimal;

public record StationeryReadOnlyDTO(

        Long id,

        String title,

        String sku,

        BigDecimal price,

        BigDecimal discountPrice,

        String dimensions,

        Availability availability,

        String company,

        String imageUrl,

        String description,

        String categoryName
) implements ProductDetailDTO{
}
