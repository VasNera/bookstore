package com.neratzis.bookstore.specification;

import com.neratzis.bookstore.dto.ProductFilterDTO;
import com.neratzis.bookstore.model.Book;
import com.neratzis.bookstore.model.Music;
import com.neratzis.bookstore.model.Product;
import com.neratzis.bookstore.model.Stationery;
import com.neratzis.bookstore.model.Toy;
import com.neratzis.bookstore.model.enums.ProductType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> build(ProductFilterDTO filters) {

        List<Specification<Product>> specs = new ArrayList<>();
        specs.add(notDeleted());

        if (filters.search() != null && !filters.search().isBlank()) {
            specs.add(titleContains(filters.search()));
        }
        if (filters.categoryId() != null) {
            specs.add(hasCategory(filters.categoryId()));
        }
        if (filters.minPrice() != null) {
            specs.add(priceAtLeast(filters.minPrice()));
        }
        if (filters.maxPrice() != null) {
            specs.add(priceAtMost(filters.maxPrice()));
        }
        if (Boolean.TRUE.equals(filters.featured())) {
            specs.add(isFeatured());
        }
        if (Boolean.TRUE.equals(filters.onSale())) {
            specs.add(onSale());
        }
        if (filters.productType() != null) {
            specs.add(ofType(filters.productType()));
        }

        return Specification.allOf(specs);
    }

    private static Specification<Product> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    private static Specification<Product> titleContains(String text) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + text.toLowerCase() + "%");
    }

    private static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    private static Specification<Product> priceAtLeast(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    private static Specification<Product> priceAtMost(BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), max);
    }

    private static Specification<Product> isFeatured() {
        return (root, query, cb) -> cb.isTrue(root.get("featured"));
    }

    private static Specification<Product> onSale() {
        return (root, query, cb) -> cb.isNotNull(root.get("discountPrice"));
    }

    private static Specification<Product> ofType(ProductType type) {
        Class<? extends Product> clazz = switch (type) {
            case BOOK -> Book.class;
            case MUSIC -> Music.class;
            case TOY -> Toy.class;
            case STATIONERY -> Stationery.class;
        };
        return (root, query, cb) -> cb.equal(root.type(), clazz);
    }
}