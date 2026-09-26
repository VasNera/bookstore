package com.neratzis.bookstore.repository;

import com.neratzis.bookstore.model.Stationery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StationeryRepository extends JpaRepository<Stationery, Long> {

    Page<Stationery> findAllByCompanyAndDeletedFalse(String company, Pageable pageable);

    Page<Stationery> findAllByFeaturedTrueAndDeletedFalse(Pageable pageable);

}
