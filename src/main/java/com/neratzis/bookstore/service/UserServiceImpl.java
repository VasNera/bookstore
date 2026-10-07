package com.neratzis.bookstore.service;

import com.neratzis.bookstore.core.exceptions.AppGenericException;
import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.dto.UserInsertDTO;
import com.neratzis.bookstore.dto.UserReadOnlyDTO;
import com.neratzis.bookstore.dto.UserUpdateDTO;
import com.neratzis.bookstore.mapper.UserMapper;
import com.neratzis.bookstore.model.Cart;
import com.neratzis.bookstore.model.Role;
import com.neratzis.bookstore.model.User;
import com.neratzis.bookstore.repository.CartRepository;
import com.neratzis.bookstore.repository.RoleRepository;
import com.neratzis.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public UserReadOnlyDTO createUser(UserInsertDTO userInsertDTO, String roleName)
            throws EntityAlreadyExistsException {
        if (userRepository.existsByUsernameAndDeletedFalse(userInsertDTO.username())) {
            throw new EntityAlreadyExistsException("USER", "User with username "
                    + userInsertDTO.username() + " already exists");
        }
        if (userRepository.existsByEmailAndDeletedFalse(userInsertDTO.email())) {
            throw new EntityAlreadyExistsException("USER", "User with email "
                    + userInsertDTO.email() + "already exists");
        }
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role " + roleName + " does not exist"));
        User user = userMapper.mapToUserEntity(userInsertDTO);
        user.setPassword(passwordEncoder.encode(userInsertDTO.password()));
        user.setRole(role);
        User savedUser = userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(savedUser);
        cartRepository.save(cart);
        log.info("Created user ={} with role = {} ", savedUser.getUsername(), roleName);
        return userMapper.mapToUserReadOnlyDTO(savedUser);

    }

    @Override
    @Transactional(readOnly = true)
    public UserReadOnlyDTO getUserByUUID(UUID uuid) throws EntityNotFoundException {
        User user = userRepository.findByUuidAndDeletedFalse(uuid)
                .orElseThrow(()-> new EntityNotFoundException("USER", "User with uuid: "
                        + uuid + " not found"));
        return userMapper.mapToUserReadOnlyDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserReadOnlyDTO getUserByUsername(String username) throws EntityNotFoundException {
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(()-> new EntityNotFoundException("USER", "User with username: "
                + username + " not found"));
        return userMapper.mapToUserReadOnlyDTO(user);
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public UserReadOnlyDTO updateUser(UserUpdateDTO updateDTO, String username)
            throws EntityNotFoundException, EntityAlreadyExistsException {
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new EntityNotFoundException("USER", "User with username: "
                        + username + " not found"));

        if (!user.getEmail().equals(updateDTO.email())
                && userRepository.existsByEmailAndDeletedFalse(updateDTO.email())) {
            throw new EntityAlreadyExistsException("USER", "User with email: "
                    + updateDTO.email() + " already exists");
        }

        userMapper.updateUserFromDTO(user, updateDTO);

        if (updateDTO.password() != null && !updateDTO.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(updateDTO.password()));
        }
            log.info("Updated user={} ", username);
            return userMapper.mapToUserReadOnlyDTO(user);

        }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public void changePassword(String username, String oldPassword, String newPassword)
            throws EntityNotFoundException, InvalidArgumentException {
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(()-> new EntityNotFoundException("USER", "User with username: "
                + username + " not found"));
        if (!passwordEncoder.matches(oldPassword,user.getPassword())){
            throw new InvalidArgumentException("PASSWORD", "Password is invalid");
           }
        user.setPassword(passwordEncoder.encode(newPassword));
        log.info("Password changed for user ={}" , username);

        }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public void deleteUser(UUID uuid) throws EntityNotFoundException {
        User user = userRepository.findByUuidAndDeletedFalse(uuid)
                .orElseThrow(()-> new EntityNotFoundException("USER", "User with uuid: "
                        + uuid + " not found"));
        user.softDelete();
        log.info("User with uuid ={} soft-deleted" , uuid);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserReadOnlyDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAllByDeletedFalse(pageable)
                .map(userMapper::mapToUserReadOnlyDTO);

    }
}
