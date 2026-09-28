package com.neratzis.bookstore.dto;

import java.util.List;

public record CategoryReadOnlyDTO(

        Long id,

        String name,

        List<CategoryReadOnlyDTO> subCategories
) {
}
