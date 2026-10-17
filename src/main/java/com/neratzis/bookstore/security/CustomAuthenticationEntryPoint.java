package com.neratzis.bookstore.security;


import com.neratzis.bookstore.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException e) throws IOException {

        log.warn("User is not authenticated, with message={}", e.getMessage());

        String errorCode = switch (e.getClass().getSimpleName()) {
            case "BadCredentialsException" -> "BAD_CREDENTIALS";
            case "CredentialsExpiredException" -> "CREDENTIALS_EXPIRED";
            case "AccountExpiredException" -> "ACCOUNT_EXPIRED";
            case "DisabledException" -> "ACCOUNT_DISABLED";
            case "LockedException" -> "ACCOUNT_LOCKED";
            default -> "UNAUTHORIZED";
        };

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        new ErrorResponseDTO(errorCode, e.getMessage())
                )
        );
    }
}
