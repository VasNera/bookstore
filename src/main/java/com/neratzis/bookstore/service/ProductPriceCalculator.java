package com.neratzis.bookstore.service;


import com.neratzis.bookstore.model.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductPriceCalculator {

    public BigDecimal effectivePrice(Product product) {
        if (product.getDiscountPrice() != null
                && product.getDiscountPrice().compareTo(product.getPrice()) < 0) {
            return product.getDiscountPrice();
        }

        return product.getPrice();
    }

    public BigDecimal lineTotal(Product product,int quantity){
        return effectivePrice(product).multiply(BigDecimal.valueOf(quantity));
    }

}
