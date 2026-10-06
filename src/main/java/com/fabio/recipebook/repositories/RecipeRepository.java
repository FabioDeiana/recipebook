package com.fabio.recipebook.repositories;

import com.fabio.recipebook.entities.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long>, JpaSpecificationExecutor<Recipe> {

    Optional<Recipe> findBySlug(String slug);

    boolean existsBySlug(String slug);

    // Slug check on update, ignoring the recipe being renamed
    boolean existsBySlugAndIdNot(String slug, Long id);

    long countByCategoryId(Long categoryId);
}
