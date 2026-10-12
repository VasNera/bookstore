package com.neratzis.bookstore.dto;

import com.neratzis.bookstore.model.enums.ProductType;

import java.math.BigDecimal;

public record ProductFilterDTO(

        String search,

        Long categoryId,

        BigDecimal minPrice,

        BigDecimal maxPrice,

        Boolean featured,

        Boolean onSale,

        ProductType productType

) {
}
