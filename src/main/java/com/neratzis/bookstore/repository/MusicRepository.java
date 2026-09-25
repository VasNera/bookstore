package com.neratzis.bookstore.repository;

import com.neratzis.bookstore.model.Category;
import com.neratzis.bookstore.model.Music;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;


public interface MusicRepository extends JpaRepository<Music,Long> {

    Page<Music> findAllByDeletedFalse(Pageable pageable);

    Page<Music> findByArtistAndDeletedFalse(String artist, Pageable pageable);

    Page<Music> findByProductionCompanyAndDeletedFalse(String productionCompany, Pageable pageable);

    Page<Music> findByTitleContainingIgnoreCaseAndDeletedFalse(String title, Pageable pageable);

    Page<Music> findAllByPriceBetweenAndDeletedFalse(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Page<Music> findAllByFeaturedTrueAndDeletedFalse(Pageable pageable);

    Page<Music> findAllByDiscountPriceIsNotNullAndDeletedFalse(Pageable pageable);

    Page<Music> findAllByCategoryAndDeletedFalse(
            Category category,
            Pageable pageable
    );
}
