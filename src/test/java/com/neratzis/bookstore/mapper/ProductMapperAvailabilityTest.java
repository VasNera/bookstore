package com.neratzis.bookstore.mapper;

import com.neratzis.bookstore.model.Book;
import com.neratzis.bookstore.model.Product;
import com.neratzis.bookstore.model.enums.Availability;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperAvailabilityTest {

    private final ProductMapper mapper = new ProductMapper();

    @ParameterizedTest(name = "stock {0} -> {1}")
    @CsvSource({
            "-1, OUT_OF_STOCK",
            "0,  OUT_OF_STOCK",
            "1,  LOW_STOCK",
            "5,  LOW_STOCK",
            "6,  IN_STOCK",
            "100, IN_STOCK"
    })
    void summaryAvailability_dependsOnStock(int stock, Availability expected) {
        Product product = new Book();
        product.setId(1L);
        product.setTitle("Test");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(stock);

        assertThat(mapper.toSummaryDTO(product).availability()).isEqualTo(expected);
    }
}