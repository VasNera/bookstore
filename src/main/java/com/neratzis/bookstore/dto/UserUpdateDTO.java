package com.neratzis.bookstore.dto;

public record UserUpdateDTO(

        String password,

        String firstname,

        String lastname,

        String phone,

        String email

) {
}
