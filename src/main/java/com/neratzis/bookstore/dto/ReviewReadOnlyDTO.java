package com.neratzis.bookstore.dto;

import java.time.Instant;

public record ReviewReadOnlyDTO(

        Long id,

        String comment,

        int rating,

        String authorName,

        Instant createdAt,

        String productTitle
) {
}
