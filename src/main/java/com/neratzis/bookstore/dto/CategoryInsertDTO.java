package com.neratzis.bookstore.dto;

public record CategoryInsertDTO(

        String name,

        Long parentCategoryId
) {
}
