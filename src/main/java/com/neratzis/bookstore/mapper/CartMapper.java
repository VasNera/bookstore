package com.neratzis.bookstore.mapper;


import com.neratzis.bookstore.dto.CartItemInsertDTO;
import com.neratzis.bookstore.dto.CartItemReadOnlyDTO;
import com.neratzis.bookstore.dto.CartReadOnlyDTO;
import com.neratzis.bookstore.model.Cart;
import com.neratzis.bookstore.model.CartItem;
import com.neratzis.bookstore.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CartMapper {

    private final ProductMapper productMapper;

    public CartItem mapToCartItemEntity(CartItemInsertDTO cartItemInsertDTO, Product product){
        CartItem cartItem = new CartItem();

        cartItem.setProduct(product);
        cartItem.setQuantity(cartItemInsertDTO.quantity());
        return cartItem;

    }

    public CartReadOnlyDTO toCartDTO(Cart cart){
        List<CartItemReadOnlyDTO> items = cart.getCartItems()
                .stream()
                .map(cartItem -> {
                    BigDecimal subtotal = cartItem.getProduct()
                            .getPrice()
                            .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                    return new CartItemReadOnlyDTO(
                            cartItem.getId(),
                            productMapper.toSummaryDTO(cartItem.getProduct()),
                            cartItem.getQuantity(),
                            subtotal
                    );

                })
                .toList();

        BigDecimal totalAmount = items.stream()
                .map(CartItemReadOnlyDTO :: subtotal)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        return new CartReadOnlyDTO(
                cart.getId(),
                items,
                totalAmount
        );
    }

}
