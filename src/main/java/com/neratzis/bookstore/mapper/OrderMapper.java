package com.neratzis.bookstore.mapper;


import com.neratzis.bookstore.dto.OrderInsertDTO;
import com.neratzis.bookstore.dto.OrderItemReadOnlyDTO;
import com.neratzis.bookstore.dto.OrderReadOnlyDTO;
import com.neratzis.bookstore.dto.OrderSummaryDTO;
import com.neratzis.bookstore.model.Order;
import com.neratzis.bookstore.model.OrderItem;
import com.neratzis.bookstore.model.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Component
public class OrderMapper {


    public OrderSummaryDTO toSummaryDTO(Order order){
        return new OrderSummaryDTO(
                order.getOrderNumber(),
                order.getCreatedAt(),
                order.getOrderStatus(),
                order.getPaymentStatus(),
                order.getTotalAmount()
        );
    }

    public OrderReadOnlyDTO toReadOnlyDTO(Order order){

        List<OrderItemReadOnlyDTO> items = order.getOrderItems()
                .stream()
                .sorted(Comparator.comparing(OrderItem::getId))
                .map(this::toOrderItemDTO)
                .toList();

        return new OrderReadOnlyDTO(
                order.getOrderNumber(),
                order.getCreatedAt(),
                order.getDocumentType(),
                order.getOrderStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getBillingBusinessName(),
                order.getBillingTaxNumber(),
                order.getBillingStreet(),
                order.getBillingProfession(),
                order.getBillingTaxOffice(),
                order.getShippingStreet(),
                order.getShippingCity(),
                order.getShippingPostalCode(),
                order.getNotes(),
                order.getTotalAmount(),
                items

        );
    }

    public OrderItemReadOnlyDTO toOrderItemDTO(OrderItem orderItem){

        BigDecimal subtotal = orderItem.getPriceAtPurchase()
                .multiply(BigDecimal.valueOf(orderItem.getQuantity()));


        return new OrderItemReadOnlyDTO(
                orderItem.getProduct().getId(),
                orderItem.getProductTitle(),
                orderItem.getPriceAtPurchase(),
                orderItem.getQuantity(),
                subtotal
        );
    }

    public Order mapToOrderEntity(OrderInsertDTO orderInsertDTO, User user){
        Order order = new Order();

        order.setDocumentType(orderInsertDTO.documentType());
        order.setPaymentMethod(orderInsertDTO.paymentMethod());

        order.setBillingBusinessName(orderInsertDTO.billingBusinessName());
        order.setBillingProfession(orderInsertDTO.billingProfession());
        order.setBillingStreet(orderInsertDTO.billingStreet());
        order.setBillingTaxNumber(orderInsertDTO.billingTaxNumber());
        order.setBillingTaxOffice(orderInsertDTO.billingTaxOffice());

        order.setShippingCity(orderInsertDTO.shippingCity());
        order.setShippingStreet(orderInsertDTO.shippingStreet());
        order.setShippingPostalCode(orderInsertDTO.shippingPostalCode());

        order.setNotes(orderInsertDTO.notes());
        order.setUser(user);

        return order;
    }
}
