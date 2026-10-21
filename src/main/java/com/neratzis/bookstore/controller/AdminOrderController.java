package com.neratzis.bookstore.controller;


import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.core.exceptions.ValidationException;
import com.neratzis.bookstore.dto.OrderPaymentStatusUpdateDTO;
import com.neratzis.bookstore.dto.OrderReadOnlyDTO;
import com.neratzis.bookstore.dto.OrderStatusUpdateDTO;
import com.neratzis.bookstore.dto.OrderSummaryDTO;
import com.neratzis.bookstore.model.enums.OrderStatus;
import com.neratzis.bookstore.service.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AdminOrderController {

    private final IOrderService orderService;

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

    @GetMapping
    public ResponseEntity<Page<OrderSummaryDTO>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {
        return new ResponseEntity<>(orderService.getAllOrders(status, pageable), HttpStatus.OK);
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderReadOnlyDTO> getOrder(@PathVariable String orderNumber)
            throws EntityNotFoundException {
        return new ResponseEntity<>(orderService.getOrderByNumberAsAdmin(orderNumber), HttpStatus.OK);
    }

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
