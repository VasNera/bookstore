package com.neratzis.bookstore.core.exceptions;

public class EntityNotFoundException extends AppGenericException{

    private static final String DEFAULT_CODE = "_ENTITY_NOT_FOUND";

    public EntityNotFoundException(String code, String message) {
        super(code + DEFAULT_CODE, message);
    }
}
