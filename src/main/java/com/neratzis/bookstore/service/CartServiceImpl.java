package com.neratzis.bookstore.service;


import com.neratzis.bookstore.core.exceptions.AppGenericException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.dto.CartItemInsertDTO;
import com.neratzis.bookstore.dto.CartReadOnlyDTO;
import com.neratzis.bookstore.mapper.CartMapper;
import com.neratzis.bookstore.model.Cart;
import com.neratzis.bookstore.model.CartItem;
import com.neratzis.bookstore.model.Product;
import com.neratzis.bookstore.repository.CartRepository;
import com.neratzis.bookstore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements ICartService{

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartReadOnlyDTO getCart(String username) throws EntityNotFoundException {
        return cartMapper.toCartDTO(findCart(username));
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public CartReadOnlyDTO addItemToCart(String username, CartItemInsertDTO cartItemInsertDTO)
            throws EntityNotFoundException, InvalidArgumentException {

        Cart cart = findCart(username);
        Product product = findActiveProduct(cartItemInsertDTO.productId());

        Optional<CartItem> existing = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        int newQuantity = existing.map(CartItem::getQuantity).orElse(0)
                + cartItemInsertDTO.quantity();

        if (newQuantity > product.getStock()) {
            throw new InvalidArgumentException("CART",
                    " Available only " + product.getStock() + " items");
        }

        if (existing.isPresent()) {
            existing.get().setQuantity(newQuantity);
        } else {
            cart.addCartItem(cartMapper.mapToCartItemEntity(cartItemInsertDTO, product));
        }

        log.info("Product with sku={} added to cart of user={}", product.getSku(), username);
        return cartMapper.toCartDTO(cart);
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public CartReadOnlyDTO updateItemQuantity(String username, Long productId, int quantity) throws EntityNotFoundException, InvalidArgumentException {
        if (quantity <= 0){
            throw new InvalidArgumentException("CART", "Cart must have positive quantity");
        }


        Cart cart = findCart(username);
        CartItem item = findItem(cart,productId);
        if (quantity > item.getProduct().getStock()) {
            throw new InvalidArgumentException("CART",
                    "Available only " + item.getProduct().getStock() + " items");
        }
        item.setQuantity(quantity);
        return cartMapper.toCartDTO(cart);
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public CartReadOnlyDTO removeItem(String username, Long productId) throws EntityNotFoundException {
        Cart cart = findCart(username);
        cart.removeCartItem(findItem(cart,productId));
        return cartMapper.toCartDTO(cart);
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public void clearCart(String username) throws EntityNotFoundException {
        Cart cart = findCart(username);
        cart.getCartItems().clear();
        log.info("Cart of user={} is empty" , username);

    }

    //Helpers

    private Cart findCart(String username) throws EntityNotFoundException{
        return cartRepository.findByUserUsername(username)
                .orElseThrow(()-> new EntityNotFoundException("CART","Cart for user " + username
                        + " not found"));
    }

    private Product findActiveProduct(Long productId)throws EntityNotFoundException{
        return productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(()-> new EntityNotFoundException("PRODUCT", "Product with id: " + productId
                + " not found"));
    }

    private CartItem findItem(Cart cart, Long productId) throws EntityNotFoundException{
        return cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(()-> new EntityNotFoundException("CART_ITEM", "Product with id: "
                +productId + " is not in the cart"));
    }
}
