package com.neratzis.bookstore.dto;

import java.math.BigDecimal;

public record CartItemReadOnlyDTO (

        Long id,

        ProductSummaryDTO product,

        int quantity,

        BigDecimal subtotal

) {
}
