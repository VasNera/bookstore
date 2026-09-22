package com.neratzis.bookstore.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "books")

public class Book extends Product{

    @Column(nullable = false,unique = true)
    private String isbn;

    @Column(nullable = false)
    private Integer pages;

    private String author;

    private Integer releaseYear;

    private String publisher;

}
