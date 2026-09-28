package com.neratzis.bookstore.dto;

import java.math.BigDecimal;

public record MusicInsertDTO(

        String title,

        String sku,

        BigDecimal price,

        BigDecimal discountPrice,

        int stock,

        String artist,

        String productionCompany,

        String imageUrl,

        String description,

        Long categoryId,

        boolean featured

) {
}
