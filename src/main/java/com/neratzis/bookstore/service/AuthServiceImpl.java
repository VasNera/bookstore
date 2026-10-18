package com.neratzis.bookstore.service;


import com.neratzis.bookstore.authentication.JwtService;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.dto.AuthRequestDTO;
import com.neratzis.bookstore.dto.AuthResponseDTO;
import com.neratzis.bookstore.model.User;
import com.neratzis.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService{

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;



    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO authenticate(AuthRequestDTO authRequestDTO) throws EntityNotFoundException {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequestDTO.username(),
                        authRequestDTO.password()
                )
        );
        User user = userRepository.findByUsernameAndDeletedFalse(authRequestDTO.username())
                .orElseThrow(()-> new EntityNotFoundException("USER", "User with username: "
                        + authRequestDTO.username() + " not found"));

        String roleName = user.getRole().getName();
        String token = jwtService.generateToken(user.getUsername(), roleName);

        log.info("User={} authenticated successfully", authRequestDTO.username());
        return new AuthResponseDTO(user.getUsername(),roleName ,token);


    }
}
