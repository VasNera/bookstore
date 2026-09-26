package com.neratzis.bookstore.repository;

import com.neratzis.bookstore.model.Category;
import com.neratzis.bookstore.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Page<Product> findAllByDeletedFalse(Pageable pageable);

    Optional<Product> findBySkuAndDeletedFalse(String sku);

    Page<Product> findByTitleContainingIgnoreCaseAndDeletedFalse(String title, Pageable pageable);

    Page<Product> findAllByCategoryAndDeletedFalse(Category category, Pageable pageable);

    Page<Product> findAllByFeaturedTrueAndDeletedFalse(Pageable pageable);

    Page<Product> findAllByDiscountPriceIsNotNullAndDeletedFalse(Pageable pageable);

    boolean existsByCategoryAndDeletedFalse(Category category);
}
