package com.neratzis.bookstore.dto;

import java.util.UUID;

public record UserReadOnlyDTO(

        UUID uuid,

        String username,

        String role

) {
}
