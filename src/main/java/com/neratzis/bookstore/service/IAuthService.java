package com.neratzis.bookstore.service;

import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.dto.AuthRequestDTO;
import com.neratzis.bookstore.dto.AuthResponseDTO;

public interface IAuthService {

    AuthResponseDTO authenticate(AuthRequestDTO authRequestDTO) throws EntityNotFoundException;
}
