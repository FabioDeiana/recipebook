package com.fabio.recipebook.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
@NoArgsConstructor
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Always stored lowercase
    @Column(nullable = false, unique = true)
    private String name;

    public Ingredient(String name) {
        setName(name);
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim().toLowerCase();
    }
}
