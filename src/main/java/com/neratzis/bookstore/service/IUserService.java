package com.neratzis.bookstore.service;

import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.dto.UserInsertDTO;
import com.neratzis.bookstore.dto.UserReadOnlyDTO;
import com.neratzis.bookstore.dto.UserUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.UUID;

public interface IUserService {

    UserReadOnlyDTO createUser(UserInsertDTO userInsertDTO, String roleName)
            throws EntityAlreadyExistsException;

    UserReadOnlyDTO updateUser(UserUpdateDTO updateDTO,String username)
        throws EntityNotFoundException,EntityAlreadyExistsException;

    UserReadOnlyDTO getUserByUUID(UUID uuid) throws EntityNotFoundException;

    UserReadOnlyDTO getUserByUsername(String username) throws EntityNotFoundException;

    public void changePassword(String username, String oldPassword, String newPassword)
            throws EntityNotFoundException, InvalidArgumentException;

    public void deleteUser(UUID uuid) throws EntityNotFoundException;

    public Page<UserReadOnlyDTO> getAllUsers(Pageable pageable);
}


