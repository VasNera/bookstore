package com.neratzis.bookstore.model;


import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractEntity {


    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at" , nullable = false, columnDefinition = "DATETIME")
    private Instant updatedAt;


    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private Instant deletedAt;

    @Column(nullable = false)
    private boolean deleted;

    public void softDelete(){
        this.deleted = true;
        this.deletedAt = Instant.now();
    }
}


