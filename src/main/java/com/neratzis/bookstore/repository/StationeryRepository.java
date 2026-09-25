package com.neratzis.bookstore.repository;

import com.neratzis.bookstore.model.Category;
import com.neratzis.bookstore.model.Stationery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface StationeryRepository extends JpaRepository<Stationery, Long> {

    Page<Stationery> findAllByDeletedFalse(Pageable pageable);

    Page<Stationery> findAllByCompanyAndDeletedFalse(String company, Pageable pageable);

    Page<Stationery> findByTitleContainingIgnoreCaseAndDeletedFalse(String title, Pageable pageable);

    Page<Stationery> findAllByPriceBetweenAndDeletedFalse(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Page<Stationery> findAllByFeaturedTrueAndDeletedFalse(Pageable pageable);

    Page<Stationery> findAllByDiscountPriceIsNotNullAndDeletedFalse(Pageable pageable);

    Page<Stationery> findAllByCategoryAndDeletedFalse(
            Category category,
            Pageable pageable
    );
}
