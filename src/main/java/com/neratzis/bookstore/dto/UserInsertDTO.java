package com.neratzis.bookstore.dto;

import jakarta.validation.constraints.*;

public record UserInsertDTO(

        @NotBlank(message = "{username.notBlank}")
        String username,

        @NotBlank(message = "{password.notBlank}")
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])^.{8,}$",
                message = "{password.pattern.invalid}")
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
