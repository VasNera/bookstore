package com.neratzis.bookstore.repository;

import com.neratzis.bookstore.model.Toy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ToyRepository extends JpaRepository<Toy, Long> {

    Page<Toy> findAllByAgeRangeAndDeletedFalse(String ageRange, Pageable pageable);

    Page<Toy> findAllByFeaturedTrueAndDeletedFalse(Pageable pageable);

}
