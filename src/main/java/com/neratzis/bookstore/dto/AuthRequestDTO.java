package com.neratzis.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AuthRequestDTO (

        @NotBlank(message = "{username.notBlank}")
        String username,

        @NotBlank(message = "{password.notBlank}")
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])^.{8,}$",
                message = "{password.pattern.invalid}")
        String password

){
}
