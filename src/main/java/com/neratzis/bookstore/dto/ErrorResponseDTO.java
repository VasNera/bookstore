package com.neratzis.bookstore.dto;

public record ErrorResponseDTO(

        String code,
        String message
)
{
    public ErrorResponseDTO(String code){
        this(code, "");
    }
}
