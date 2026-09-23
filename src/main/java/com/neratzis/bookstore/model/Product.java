package com.neratzis.bookstore.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "products")
public abstract class Product extends AbstractEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false,unique = true)
    private String sku;

    private String dimensions;

    @Column(nullable = false)
    private BigDecimal price;

    private BigDecimal discountPrice;

    @Column(nullable = false)
    private int stock;

    private String imageUrl;
    private boolean featured;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToMany(mappedBy = "favourites", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<CartItem> cartItems = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return Objects.equals(getSku(), product.getSku());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getSku());
    }
}
