package com.neratzis.bookstore.dto;

public record UserInsertDTO(

        String username,

        String password,

        String firstname,

        String lastname,

        String phone,

        String email

) {
}
