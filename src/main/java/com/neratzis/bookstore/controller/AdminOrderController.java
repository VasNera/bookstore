package com.neratzis.bookstore.controller;


import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.core.exceptions.ValidationException;
import com.neratzis.bookstore.dto.*;
import com.neratzis.bookstore.model.enums.OrderStatus;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("hasAuthority('MANAGE_ORDERS')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Admin - Orders", description = "Order and payment status management (MANAGE_ORDERS)")
@RequiredArgsConstructor
public class AdminOrderController {

    private final IOrderService orderService;

    @Operation(summary = "Change the order status",
            description = "Allowed transitions: PENDING → CONFIRMED/CANCELLED, CONFIRMED → SHIPPED/CANCELLED, SHIPPED → DELIVERED. "
                    + "Delivering a CASH_ON_DELIVERY order marks it as PAID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = OrderReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or transition not allowed",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PatchMapping("/{orderNumber}/status")
    public ResponseEntity<OrderReadOnlyDTO> updateStatus(
            @PathVariable String orderNumber,
            @Valid @RequestBody
            OrderStatusUpdateDTO updateDTO,
            BindingResult bindingResult
    )throws ValidationException, EntityNotFoundException, InvalidArgumentException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("ORDER", "Invalid status data", bindingResult);
        }
        return new ResponseEntity<>(orderService.updateOrderStatus(orderNumber,updateDTO.orderStatus()), HttpStatus.OK);
    }

    @Operation(summary = "List all orders",
            description = "Paginated summaries of every customer's orders, newest first. Optional filter by order status.")
    @ApiResponse(responseCode = "200", description = "Orders returned")

    @GetMapping
    public ResponseEntity<Page<OrderSummaryDTO>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @ParameterObject
            Pageable pageable) {
        return new ResponseEntity<>(orderService.getAllOrders(status, pageable), HttpStatus.OK);
    }

    @Operation(summary = "Get any order",
            description = "Full details of any customer's order. There is no ownership check for admins.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderReadOnlyDTO.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })

    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderReadOnlyDTO> getOrder(@PathVariable String orderNumber)
            throws EntityNotFoundException {
        return new ResponseEntity<>(orderService.getOrderByNumberAsAdmin(orderNumber), HttpStatus.OK);
    }

    @Operation(summary = "Change the payment status",
            description = "Allowed transitions: PENDING → PAID/FAILED, PAID → REFUNDED. Not allowed on cancelled orders.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment status updated",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = OrderReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error, cancelled order or transition not allowed",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PatchMapping("/{orderNumber}/payment-status")
    public ResponseEntity<OrderReadOnlyDTO> updatePaymentStatus(
            @PathVariable String orderNumber,
            @Valid @RequestBody OrderPaymentStatusUpdateDTO updateDTO,
            BindingResult bindingResult
    ) throws ValidationException, EntityNotFoundException, InvalidArgumentException {
        if (bindingResult.hasErrors()) {
            throw new ValidationException("ORDER", "Invalid payment status data", bindingResult);
        }
        return new ResponseEntity<>(
                orderService.updatePaymentStatus(orderNumber, updateDTO.paymentStatus()), HttpStatus.OK);
    }

}
