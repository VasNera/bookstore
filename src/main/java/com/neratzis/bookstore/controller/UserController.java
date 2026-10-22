package com.neratzis.bookstore.controller;


import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.core.exceptions.ValidationException;
import com.neratzis.bookstore.dto.*;
import com.neratzis.bookstore.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Users", description = "Own profile")
public class UserController {

    private final IUserService userService;

    @Operation(summary = "Get my profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserReadOnlyDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @GetMapping("/me")
    public ResponseEntity<UserReadOnlyDTO> getMyProfile(Authentication authentication) throws EntityNotFoundException{
        return new ResponseEntity<>(userService.getUserByUsername(authentication.getName()), HttpStatus.OK);
    }

    @Operation(summary = "Update my profile",
            description = "The username cannot be changed. The user is always taken from the token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ValidationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Email already in use",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PutMapping("/me")
    public ResponseEntity<UserReadOnlyDTO> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody
            UserUpdateDTO updateDTO, BindingResult bindingResult)
            throws ValidationException, EntityNotFoundException, EntityAlreadyExistsException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("USER", "Invalid user data", bindingResult);
        }
        return new ResponseEntity<>(userService.updateUser(authentication.getName(), updateDTO),HttpStatus.OK);
    }

    @Operation(summary = "Change my password", description = "Requires the current password.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Validation error or wrong current password",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody
            ChangePasswordDTO passwordDTO, BindingResult bindingResult)
            throws ValidationException,EntityNotFoundException, InvalidArgumentException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("USER", "Invalid password data", bindingResult);
        }
        userService.changePassword(authentication.getName(),
                passwordDTO.oldPassword(),
                passwordDTO.newPassword());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
