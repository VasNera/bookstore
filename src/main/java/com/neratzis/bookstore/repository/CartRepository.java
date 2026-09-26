package com.neratzis.bookstore.repository;

import com.neratzis.bookstore.model.Cart;
import com.neratzis.bookstore.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = {"cartItems", "cartItems.product"})
    Optional<Cart> findByUserUsername(String username);


}
