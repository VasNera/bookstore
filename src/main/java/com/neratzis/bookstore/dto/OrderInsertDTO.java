package com.neratzis.bookstore.dto;

import com.neratzis.bookstore.model.enums.DocumentType;
import com.neratzis.bookstore.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderInsertDTO(

        @NotNull(message = "{documentType.notNull}")
        DocumentType documentType,

        @NotNull(message = "{paymentMethod.notNull}")
        PaymentMethod paymentMethod,

        String billingBusinessName,

        String billingTaxNumber,

        String billingStreet,

        String billingProfession,

        String billingTaxOffice,

        @NotBlank(message = "{shippingStreet.notBlank}")
        String shippingStreet,

        @NotBlank(message = "{shippingCity.notBlank}")
        String shippingCity,

        @NotBlank(message = "{shippingPostalCode.notBlank}")
        String shippingPostalCode,

        String notes
) {
}
