package com.neratzis.bookstore.repository;

import com.neratzis.bookstore.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"role", "role.capabilities"})
    Optional<User> findByUsernameAndDeletedFalse(String username);

    Optional<User> findByUuidAndDeletedFalse (UUID uuid);

    Optional<User> findByEmailAndDeletedFalse(String email);

    Page<User> findAllByDeletedFalse(Pageable pageable);

    boolean existsByUsernameAndDeletedFalse(String username);

}
