package com.neratzis.bookstore.dto;

import com.neratzis.bookstore.model.enums.Availability;

import java.math.BigDecimal;

public record ToyReadOnlyDTO(

        Long id,

        String title,

        String sku,

        BigDecimal price,

        BigDecimal discountPrice,

        String dimensions,

        String ageRange,

        Availability availability,

        String imageUrl,

        String description,

        String categoryName

) implements ProductDetailDTO{
}
