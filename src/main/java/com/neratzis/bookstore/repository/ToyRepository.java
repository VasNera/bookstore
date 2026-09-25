package com.neratzis.bookstore.repository;

import com.neratzis.bookstore.model.Category;
import com.neratzis.bookstore.model.Toy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface ToyRepository extends JpaRepository<Toy, Long> {

    Page<Toy> findAllByDeletedFalse(Pageable pageable);

    Page<Toy> findAllByAgeRangeAndDeletedFalse(String ageRange, Pageable pageable);

    Page<Toy> findByTitleContainingIgnoreCaseAndDeletedFalse(String title, Pageable pageable);

    Page<Toy> findAllByPriceBetweenAndDeletedFalse(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Page<Toy> findAllByFeaturedTrueAndDeletedFalse(Pageable pageable);

    Page<Toy> findAllByDiscountPriceIsNotNullAndDeletedFalse(Pageable pageable);

    Page<Toy> findAllByCategoryAndDeletedFalse(
            Category category,
            Pageable pageable
    );
}
