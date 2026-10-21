package com.neratzis.bookstore.controller;


import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.core.exceptions.ValidationException;
import com.neratzis.bookstore.dto.OrderInsertDTO;
import com.neratzis.bookstore.dto.OrderReadOnlyDTO;
import com.neratzis.bookstore.dto.OrderSummaryDTO;
import com.neratzis.bookstore.service.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class OrderController {

    private final IOrderService orderService;

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

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_OWN_ORDERS')")
    public ResponseEntity<Page<OrderSummaryDTO>> getMyOrders(
            Authentication authentication,
            @PageableDefault(size = 10)Pageable pageable){
        return new ResponseEntity<>(orderService.getMyOrders(authentication.getName(),pageable), HttpStatus.OK);
    }

    @GetMapping("/{orderNumber}")
    @PreAuthorize("hasAuthority('VIEW_OWN_ORDERS')")
    public ResponseEntity<OrderReadOnlyDTO> getOrderByNumber(Authentication authentication
            ,@PathVariable String orderNumber)throws EntityNotFoundException,InvalidArgumentException{
        return new ResponseEntity<>(orderService.getOrderByNumber(authentication.getName(), orderNumber),HttpStatus.OK);
    }

    @PatchMapping("/{orderNumber}/cancel")
    @PreAuthorize("hasAuthority('CANCEL_OWN_ORDERS')")
    public ResponseEntity<OrderReadOnlyDTO> cancelOrder(Authentication authentication
            ,@PathVariable String orderNumber)throws EntityNotFoundException, InvalidArgumentException{
        return new ResponseEntity<>(orderService.cancelOrder(authentication.getName(),orderNumber ),HttpStatus.OK);
    }


}
