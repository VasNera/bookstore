package com.neratzis.bookstore.dto;

public record CartItemInsertDTO(

        Long productId,

        int quantity

) {
}
