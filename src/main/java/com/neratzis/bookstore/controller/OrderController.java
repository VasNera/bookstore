package com.neratzis.bookstore.controller;


import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.core.exceptions.ValidationException;
import com.neratzis.bookstore.dto.*;
import com.neratzis.bookstore.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Orders", description = "Checkout and own orders")
public class OrderController {

    private final IOrderService orderService;

    @Operation(summary = "Place an order (checkout)",
            description = "Creates an order from the current user's cart, decreases the stock, empties the cart and generates the order number. "
                    + "INVOICE requires business name, tax number, street and profession. The user, items, total and status are never taken from the client.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = OrderReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error, empty cart, missing invoice data, product not available or not enough stock",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ValidationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User or cart not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Concurrent stock modification (optimistic locking), try again",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_ORDER')")
    public ResponseEntity<OrderReadOnlyDTO> checkout(
            Authentication authentication,
            @Valid @RequestBody
            OrderInsertDTO insertDTO, BindingResult bindingResult
            ) throws ValidationException, EntityNotFoundException, InvalidArgumentException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("ORDER", "Invalid order data", bindingResult);
        }
        return new ResponseEntity<>(orderService.createOrder(authentication.getName(), insertDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "List my orders",
            description = "Paginated summaries of the logged-in user's orders, newest first. Items are not included.")
    @ApiResponse(responseCode = "200", description = "Orders returned")

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_OWN_ORDERS')")
    public ResponseEntity<Page<OrderSummaryDTO>> getMyOrders(
            Authentication authentication,
            @ParameterObject
            @PageableDefault(size = 10)Pageable pageable){
        return new ResponseEntity<>(orderService.getMyOrders(authentication.getName(),pageable), HttpStatus.OK);
    }

    @Operation(summary = "Get one of my orders",
            description = "Full details with items and price snapshots. Only the owner can see the order.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order returned",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = OrderReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "The order does not belong to the user",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @GetMapping("/{orderNumber}")
    @PreAuthorize("hasAuthority('VIEW_OWN_ORDERS')")
    public ResponseEntity<OrderReadOnlyDTO> getOrderByNumber(Authentication authentication
            ,@PathVariable String orderNumber)throws EntityNotFoundException,InvalidArgumentException{
        return new ResponseEntity<>(orderService.getOrderByNumber(authentication.getName(), orderNumber),HttpStatus.OK);
    }

    @Operation(summary = "Cancel my order",
            description = "Restores the stock. Only before the order is shipped. If the order was already paid, the payment status becomes REFUNDED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order cancelled",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = OrderReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Not the user's order, already cancelled, or already shipped/delivered",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PatchMapping("/{orderNumber}/cancel")
    @PreAuthorize("hasAuthority('CANCEL_OWN_ORDERS')")
    public ResponseEntity<OrderReadOnlyDTO> cancelOrder(Authentication authentication
            ,@PathVariable String orderNumber)throws EntityNotFoundException, InvalidArgumentException{
        return new ResponseEntity<>(orderService.cancelOrder(authentication.getName(),orderNumber ),HttpStatus.OK);
    }
}
