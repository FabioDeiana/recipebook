package com.fabio.recipebook.repositories;

import com.fabio.recipebook.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    // Removes the tag from every recipe before the tag itself is deleted
    @Modifying
    @Query(value = "DELETE FROM recipe_tags WHERE tag_id = :tagId", nativeQuery = true)
    void removeFromAllRecipes(@Param("tagId") Long tagId);
}
