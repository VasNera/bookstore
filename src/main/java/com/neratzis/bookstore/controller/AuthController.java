package com.neratzis.bookstore.controller;


import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.ValidationException;
import com.neratzis.bookstore.dto.*;
import com.neratzis.bookstore.service.IAuthService;
import com.neratzis.bookstore.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Register and login (public)")
public class AuthController {

    private final IUserService userService;
    private final IAuthService authService;

    @Operation(summary = "Register a customer",
            description = "Self-registration. The role is always CUSTOMER and cannot be chosen by the client. A cart is created for the new user.")

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ValidationResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Username or email already exists",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

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

    @Operation(summary = "Login", description = "Returns a JWT token to be sent as 'Authorization: Bearer <token>'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ValidationResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials (same response for unknown user and wrong password)",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

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
