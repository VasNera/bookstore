package com.neratzis.bookstore.dto;

import java.math.BigDecimal;

public record OrderItemReadOnlyDTO(

        Long productId,

        String productTitle,

        BigDecimal priceAtPurchase,

        int quantity,

        BigDecimal subtotal

) {
}
