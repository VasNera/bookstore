package com.neratzis.bookstore.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false,updatable = false,columnDefinition = "BINARY(16)")
    private UUID uuid = UUID.randomUUID();

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false,length = 50)
    private String firstname;

    @Column(nullable = false, length = 50)
    private String lastname;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false, length = 25)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_favourites",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "product_id"))
    private Set<Product> favourites = new HashSet<>();

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Cart cart;

    public void addFavourite(Product product){
        favourites.add(product);
        product.getUsers().add(this);
    }

    public void removeFavourite(Product product){
        favourites.remove(product);
        product.getUsers().remove(this);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(getUsername(), user.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUsername());
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                '}';
    }
}

