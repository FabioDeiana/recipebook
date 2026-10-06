package com.fabio.recipebook.repositories;

import com.fabio.recipebook.entities.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByNameIgnoreCase(String name);

    // Autocomplete for the recipe form
    List<Ingredient> findTop10ByNameContainingIgnoreCaseOrderByNameAsc(String search);
}
