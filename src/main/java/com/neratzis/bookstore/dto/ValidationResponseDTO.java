package com.neratzis.bookstore.dto;

import java.util.Map;

public record ValidationResponseDTO(

        String code,
        String message,
        Map<String,String> errors
) {
}
