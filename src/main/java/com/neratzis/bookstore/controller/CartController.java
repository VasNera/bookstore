package com.neratzis.bookstore.controller;

import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.core.exceptions.ValidationException;
import com.neratzis.bookstore.dto.*;
import com.neratzis.bookstore.service.ICartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@PreAuthorize("hasAuthority('MANAGE_OWN_CART')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Cart", description = "Server-side shopping cart (MANAGE_OWN_CART)")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    @Operation(summary = "Get my cart",
            description = "Returns the cart of the logged-in user with line subtotals and the total, calculated on the server.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart returned",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CartReadOnlyDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cart not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @GetMapping
    public ResponseEntity<CartReadOnlyDTO> getCart(Authentication authentication)throws EntityNotFoundException{
        return new ResponseEntity<>(cartService.getCart(authentication.getName()), HttpStatus.OK);

    }

    @Operation(summary = "Add a product to the cart",
            description = "If the product is already in the cart the quantity increases. The cart does not reserve stock; the binding check happens at checkout.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart updated",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CartReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or quantity exceeds available stock",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ValidationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

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

    @Operation(summary = "Change an item's quantity",
            description = "Sets the quantity of a product that is already in the cart. The product id comes only from the URL.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quantity updated",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CartReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid quantity or exceeds available stock",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ValidationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product is not in the cart",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PatchMapping("/items/{productId}")
    public ResponseEntity<CartReadOnlyDTO> updateQuantity(
            Authentication authentication,
            @PathVariable long productId,
            @Valid @RequestBody
            CartItemUpdateDTO updateDTO,
            BindingResult bindingResult
    ) throws ValidationException,EntityNotFoundException,InvalidArgumentException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("CART", "Invalid quantity", bindingResult);
        }
        return new ResponseEntity<>(cartService.updateItemQuantity(authentication.getName(),
                productId,updateDTO.quantity()),HttpStatus.OK);
    }

    @Operation(summary = "Remove an item from the cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item removed, updated cart returned",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CartReadOnlyDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product is not in the cart",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartReadOnlyDTO> removeItem(Authentication authentication, @PathVariable long productId)
            throws ValidationException, EntityNotFoundException,InvalidArgumentException{
        return new ResponseEntity<>(cartService.removeItem(authentication.getName(), productId)
                ,HttpStatus.OK);
    }

    @Operation(summary = "Empty the cart")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cart emptied"),
            @ApiResponse(responseCode = "404", description = "Cart not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication)throws EntityNotFoundException{
        cartService.clearCart(authentication.getName());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
