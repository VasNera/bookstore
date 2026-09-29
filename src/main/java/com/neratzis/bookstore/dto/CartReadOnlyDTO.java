package com.neratzis.bookstore.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartReadOnlyDTO (

        Long id,

        List<CartItemReadOnlyDTO> items,

        BigDecimal totalAmount

) {
}
