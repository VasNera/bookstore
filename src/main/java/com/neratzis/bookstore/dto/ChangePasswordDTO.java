package com.neratzis.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDTO(

        @NotBlank(message = "{oldPassword.notBlank}")
        String oldPassword,

        @NotBlank(message = "{newPassword.notBlank}")
        @Size(min = 8, message = "{newPassword.size}")
        String newPassword
) {
}
