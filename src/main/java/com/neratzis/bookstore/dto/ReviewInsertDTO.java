package com.neratzis.bookstore.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;


public record ReviewInsertDTO(

        @Min(value = 1, message = "{rating.min}")
        @Max(value = 5, message = "{rating.max}")
        int rating,

        String comment
) {
}
