package com.fabio.recipebook.dto.response;

import com.fabio.recipebook.entities.Recipe;
import com.fabio.recipebook.enums.Section;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

// Used in the recipe list: no ingredients or steps
public record RecipeSummaryDTO(
        Long id,
        String title,
        String slug,
        String description,
        Integer servings,
        Integer prepTimeMinutes,
        Integer cookTimeMinutes,
        boolean favorite,
        String imageUrl,
        LocalDate lastCookedAt,
        Section section,
        String authorName,
        LocalDateTime createdAt,
        CategoryResponseDTO category,
        List<TagResponseDTO> tags
) {

    public static RecipeSummaryDTO from(Recipe recipe) {
        return new RecipeSummaryDTO(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getSlug(),
                recipe.getDescription(),
                recipe.getServings(),
                recipe.getPrepTimeMinutes(),
                recipe.getCookTimeMinutes(),
                recipe.isFavorite(),
                recipe.getImageUrl(),
                recipe.getLastCookedAt(),
                recipe.getSection(),
                recipe.getAuthorName(),
                recipe.getCreatedAt(),
                recipe.getCategory() == null ? null : CategoryResponseDTO.from(recipe.getCategory()),
                recipe.getTags().stream()
                        .sorted(Comparator.comparing(tag -> tag.getName().toLowerCase()))
                        .map(TagResponseDTO::from)
                        .toList()
        );
    }
}
