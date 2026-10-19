package com.neratzis.bookstore.controller;

import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.core.exceptions.ValidationException;
import com.neratzis.bookstore.dto.CartItemInsertDTO;
import com.neratzis.bookstore.dto.CartReadOnlyDTO;
import com.neratzis.bookstore.service.ICartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    @GetMapping
    public ResponseEntity<CartReadOnlyDTO> getCart(Authentication authentication)throws EntityNotFoundException{
        return new ResponseEntity<>(cartService.getCart(authentication.getName()), HttpStatus.OK);

    }

    @PostMapping("/items")
    public ResponseEntity<CartReadOnlyDTO> addItem(
            Authentication authentication,
            @Valid @RequestBody
            CartItemInsertDTO insertDTO,
            BindingResult bindingResult
    )throws ValidationException, EntityNotFoundException, InvalidArgumentException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("CART", "Invalid cart data", bindingResult);
        }
        return new ResponseEntity<>(cartService.addItemToCart(authentication.getName(), insertDTO),HttpStatus.OK);
    }

    @PatchMapping("/items/{productId}")
    public ResponseEntity<CartReadOnlyDTO> updateQuantity(
            Authentication authentication,
            @PathVariable long productId,
            @Valid @RequestBody
            CartItemInsertDTO insertDTO,
            BindingResult bindingResult
    ) throws ValidationException,EntityNotFoundException,InvalidArgumentException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("CART", "Invalid quantity", bindingResult);
        }
        return new ResponseEntity<>(cartService.updateItemQuantity(authentication.getName(),
                productId,insertDTO.quantity()),HttpStatus.OK);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartReadOnlyDTO> removeItem(Authentication authentication, @PathVariable long productId)
            throws ValidationException, EntityNotFoundException,InvalidArgumentException{
        return new ResponseEntity<>(cartService.removeItem(authentication.getName(), productId)
                ,HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication)throws EntityNotFoundException{
        cartService.clearCart(authentication.getName());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
