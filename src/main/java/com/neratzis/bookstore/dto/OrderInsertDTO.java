package com.neratzis.bookstore.dto;

import com.neratzis.bookstore.model.enums.DocumentType;
import com.neratzis.bookstore.model.enums.PaymentMethod;

public record OrderInsertDTO(

        DocumentType documentType,

        PaymentMethod paymentMethod,

        String billingBusinessName,

        String billingTaxNumber,

        String billingStreet,

        String billingProfession,

        String billingTaxOffice,

        String shippingStreet,

        String shippingCity,

        String shippingPostalCode,

        String notes
) {
}
