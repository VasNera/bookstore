package com.neratzis.bookstore.controller;


import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.core.exceptions.ValidationException;
import com.neratzis.bookstore.dto.ChangePasswordDTO;
import com.neratzis.bookstore.dto.UserReadOnlyDTO;
import com.neratzis.bookstore.dto.UserUpdateDTO;
import com.neratzis.bookstore.service.IUserService;
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
public class UserController {

    private final IUserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserReadOnlyDTO> getMyProfile(Authentication authentication) throws EntityNotFoundException{
        return new ResponseEntity<>(userService.getUserByUsername(authentication.getName()), HttpStatus.OK);
    }

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
