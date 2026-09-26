package com.neratzis.bookstore.repository;

import com.neratzis.bookstore.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbnAndDeletedFalse(String isbn);

    Page<Book> findByAuthorContainingIgnoreCaseAndDeletedFalse(String author, Pageable pageable);

    Page<Book> findByPublisherAndDeletedFalse(String publisher,Pageable pageable);

    Page<Book> findAllByFeaturedTrueAndDeletedFalse(Pageable pageable);

}
