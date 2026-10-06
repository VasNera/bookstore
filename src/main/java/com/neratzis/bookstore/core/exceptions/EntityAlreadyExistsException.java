package com.neratzis.bookstore.core.exceptions;


public class EntityAlreadyExistsException extends AppGenericException{

    private static final String DEFAULT_CODE = "_ALREADY_EXISTS";


    public EntityAlreadyExistsException(String code, String message) {
        super(code + DEFAULT_CODE, message);
    }
}
