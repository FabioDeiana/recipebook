package com.fabio.recipebook.dto.response;

import com.fabio.recipebook.entities.Recipe;
import com.fabio.recipebook.enums.Section;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record RecipeDetailDTO(
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
        String adaptedFrom,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        CategoryResponseDTO category,
        List<TagResponseDTO> tags,
        List<RecipeIngredientResponseDTO> ingredients,
        List<StepResponseDTO> steps
) {

    public static RecipeDetailDTO from(Recipe recipe) {
        return new RecipeDetailDTO(
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
                recipe.getAdaptedFrom(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt(),
                recipe.getCategory() == null ? null : CategoryResponseDTO.from(recipe.getCategory()),
                recipe.getTags().stream()
                        .sorted(Comparator.comparing(tag -> tag.getName().toLowerCase()))
                        .map(TagResponseDTO::from)
                        .toList(),
                recipe.getIngredients().stream()
                        .map(RecipeIngredientResponseDTO::from)
                        .toList(),
                recipe.getSteps().stream()
                        .sorted(Comparator.comparing(step -> step.getStepNumber()))
                        .map(StepResponseDTO::from)
                        .toList()
        );
    }
}
