package com.neratzis.bookstore.dto;


import java.math.BigDecimal;

public record BookInsertDTO(

        String title,

        String sku,

        String isbn,

        String dimensions,

        Integer pages,

        BigDecimal price,

        BigDecimal discountPrice,

        int stock,

        String author,

        String publisher,

        Integer releaseYear,

        String imageUrl,

        String description,

        Long categoryId,

        boolean featured

) {
}
