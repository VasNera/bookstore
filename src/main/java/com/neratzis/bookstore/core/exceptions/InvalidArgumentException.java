package com.neratzis.bookstore.core.exceptions;

public class InvalidArgumentException extends AppGenericException{

    private static final String DEFAULT_CODE = "_INVALID_ARGUMENT";

    public InvalidArgumentException(String code, String message) {
        super(code + DEFAULT_CODE, message);
    }
}
