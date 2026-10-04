package com.neratzis.bookstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateDTO(

        @NotBlank(message = "{password.notBlank}")
        String password,

        @NotBlank(message = "{firstname.notBlank}")
        @Size(min = 3, max = 30, message = "{firstname.size}")
        String firstname,

        @NotBlank(message = "{lastname.notBlank}")
        @Size(min = 3, max = 30, message = "{lastname.size}")
        String lastname,

        @NotNull(message = "{phone.notNull}")
        String phone,

        @NotBlank(message = "{email.notBlank}")
        @Email(message = "{email.invalid}")
        String email

) {
}
