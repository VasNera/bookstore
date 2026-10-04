package com.neratzis.bookstore.dto;

import com.neratzis.bookstore.model.enums.Availability;

import java.math.BigDecimal;

public record BookReadOnlyDTO(

        Long id,

        String title,

        String sku,

        String dimensions,

        BigDecimal price,

        BigDecimal discountPrice,

        String imageUrl,

        Availability availability,

        String description,

        String categoryName,

        String isbn,

        Integer pages,

        String author,

        Integer releaseYear,

        String publisher
) implements ProductDetailDTO {
}
