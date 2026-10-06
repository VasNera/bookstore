package com.neratzis.bookstore.core.exceptions;

import lombok.Getter;

@Getter
public class InvalidCredentialsException extends AppGenericException{

    private static final String DEFAULT_CODE = "_INVALID_CREDENTIALS";

    public InvalidCredentialsException(String code, String message) {
        super(code + DEFAULT_CODE, message);
    }
}
