package com.neratzis.bookstore.dto;

public record CategoryUpdateDTO(
        String name,

        Long parentCategoryId
) {
}
