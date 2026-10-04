package com.neratzis.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryInsertDTO(

        @NotBlank(message = "{name.notBlank}")
        String name,

        Long parentCategoryId
) {
}
