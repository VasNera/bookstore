package com.neratzis.bookstore.service;

import com.neratzis.bookstore.model.Book;
import com.neratzis.bookstore.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductPriceCalculatorTest {

    private final ProductPriceCalculator calculator = new ProductPriceCalculator();

    private Product product(String price, String discountPrice) {
        Product p = new Book();
        p.setPrice(new BigDecimal(price));
        p.setDiscountPrice(discountPrice == null ? null : new BigDecimal(discountPrice));
        return p;
    }

    @Test
    void effectivePrice_returnsDiscount_whenDiscountIsLowerThanPrice() {
        assertThat(calculator.effectivePrice(product("14.90", "11.90")))
                .isEqualByComparingTo("11.90");
    }

    @Test
    void effectivePrice_returnsPrice_whenThereIsNoDiscount() {
        assertThat(calculator.effectivePrice(product("14.90", null)))
                .isEqualByComparingTo("14.90");
    }

    @Test
    void effectivePrice_ignoresDiscount_whenItIsNotLowerThanPrice() {
        assertThat(calculator.effectivePrice(product("14.90", "14.90")))
                .isEqualByComparingTo("14.90");
        assertThat(calculator.effectivePrice(product("14.90", "20.00")))
                .isEqualByComparingTo("14.90");
    }

    @Test
    void effectivePrice_comparesByValue_not_byScale() {
        // 14.9 and 14.90 are equal as numbers even though BigDecimal.equals says otherwise
        assertThat(calculator.effectivePrice(product("14.90", "14.9")))
                .isEqualByComparingTo("14.90");
    }

    @Test
    void lineTotal_multipliesEffectivePriceByQuantity() {
        assertThat(calculator.lineTotal(product("14.90", "11.90"), 2))
                .isEqualByComparingTo("23.80");
    }
}