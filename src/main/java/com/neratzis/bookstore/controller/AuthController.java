package com.neratzis.bookstore.controller;


import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.ValidationException;
import com.neratzis.bookstore.dto.AuthRequestDTO;
import com.neratzis.bookstore.dto.AuthResponseDTO;
import com.neratzis.bookstore.dto.UserInsertDTO;
import com.neratzis.bookstore.dto.UserReadOnlyDTO;
import com.neratzis.bookstore.service.IAuthService;
import com.neratzis.bookstore.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUserService userService;
    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserReadOnlyDTO> register(
            @Valid @RequestBody
            UserInsertDTO userInsertDTO,
            BindingResult bindingResult
            ) throws ValidationException,EntityAlreadyExistsException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("USER", "Invalid registration data", bindingResult);
        }
        UserReadOnlyDTO response = userService.createUser(userInsertDTO,"CUSTOMER");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody
            AuthRequestDTO authRequestDTO,
            BindingResult bindingResult
    ) throws ValidationException,EntityNotFoundException{
        if (bindingResult.hasErrors()){
            throw new ValidationException ("AUTH", "Invalid login data", bindingResult);
        }
        return new ResponseEntity<>(authService.authenticate(authRequestDTO),HttpStatus.OK);

    }
}
