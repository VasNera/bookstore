package com.neratzis.bookstore.repository;

import com.neratzis.bookstore.model.Product;
import com.neratzis.bookstore.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByUserUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    @EntityGraph(attributePaths = {"product"})
    Page<Review> findByProductOrderByCreatedAtDesc(Product product, Pageable pageable);

    boolean existsByUserUsernameAndProduct(String username, Product product);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product= :product")
    Double findAverageRatingByProduct(@Param("product")Product product);

    @EntityGraph(attributePaths = {"user", "product"})
    Page<Review> findByCommentIsNotNullOrderByCreatedAtDesc(Pageable pageable);

}
