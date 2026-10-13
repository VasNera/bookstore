package com.neratzis.bookstore.service;

import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.dto.CartItemInsertDTO;
import com.neratzis.bookstore.dto.CartReadOnlyDTO;

public interface ICartService {

    CartReadOnlyDTO getCart(String username) throws EntityNotFoundException;

    CartReadOnlyDTO addItemToCart(String username, CartItemInsertDTO cartItemInsertDTO)
            throws EntityNotFoundException, InvalidArgumentException;

    CartReadOnlyDTO updateItemQuantity(String username, Long productId, int quantity)
            throws EntityNotFoundException, InvalidArgumentException;

    CartReadOnlyDTO removeItem(String username, Long productId) throws EntityNotFoundException;

    void clearCart(String username) throws EntityNotFoundException;

}
