package com.neratzis.bookstore.repository;

import com.neratzis.bookstore.model.Book;
import com.neratzis.bookstore.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Page<Book> findAllByDeletedFalse(Pageable pageable);

    Optional<Book> findByIsbnAndDeletedFalse(String isbn);

    Page<Book> findByAuthorAndDeletedFalse(String author, Pageable pageable);

    Page<Book> findByPublisherAndDeletedFalse(String publisher,Pageable pageable);

    Page<Book> findByTitleContainingIgnoreCaseAndDeletedFalse(String title, Pageable pageable);

    Page<Book> findAllByPriceBetweenAndDeletedFalse(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Page<Book> findAllByFeaturedTrueAndDeletedFalse(Pageable pageable);

    Page<Book> findAllByDiscountPriceIsNotNullAndDeletedFalse(Pageable pageable);

    Page<Book> findAllByCategoryAndDeletedFalse(
            Category category,
            Pageable pageable
    );

}
