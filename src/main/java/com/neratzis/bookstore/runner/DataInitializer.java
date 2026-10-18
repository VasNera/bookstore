package com.neratzis.bookstore.runner;

import com.neratzis.bookstore.model.Cart;
import com.neratzis.bookstore.model.Role;
import com.neratzis.bookstore.model.User;
import com.neratzis.bookstore.repository.CartRepository;
import com.neratzis.bookstore.repository.RoleRepository;
import com.neratzis.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {

        if (userRepository.existsByUsernameAndDeletedFalse("admin")) {
            return;
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setFirstname("Vasilis");
        admin.setLastname("Neratzis");
        admin.setEmail("admin@bookstore.gr");
        admin.setPhone("2100000000");
        admin.setRole(adminRole);

        User saved = userRepository.save(admin);

        Cart cart = new Cart();
        cart.setUser(saved);
        cartRepository.save(cart);

        log.info("Admin user seeded");
    }
}