package com.neratzis.bookstore.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
    @JoinTable(name = "roles_capabilities",
    joinColumns = @JoinColumn(name = "role_id"),
    inverseJoinColumns = @JoinColumn(name ="capability_id"))
    private Set<Capability> capabilities = new HashSet<>();

    public void addCapability(Capability capability){
        capabilities.add(capability);
        capability.getRoles().add(this);
    }

    public void removeCapability(Capability capability){
        capabilities.remove(capability);
        capability.getRoles().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return Objects.equals(getName(), role.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }

    @Override
    public String toString() {
        return "Role{" +
                "name='" + getName() + '\'' +
                ", id=" + getId() +
                '}';
    }
}

