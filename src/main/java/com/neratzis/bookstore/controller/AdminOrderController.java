package com.neratzis.bookstore.controller;


import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.core.exceptions.ValidationException;
import com.neratzis.bookstore.dto.OrderReadOnlyDTO;
import com.neratzis.bookstore.dto.OrderStatusUpdateDTO;
import com.neratzis.bookstore.service.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
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

}
