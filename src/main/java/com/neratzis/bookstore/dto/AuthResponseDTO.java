package com.neratzis.bookstore.dto;

public record AuthResponseDTO(

        String username,

        String role,

        String token
) {
}
