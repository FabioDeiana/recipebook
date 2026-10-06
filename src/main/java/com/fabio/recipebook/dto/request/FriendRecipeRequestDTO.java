package com.fabio.recipebook.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Set;

// Same fields as RecipeRequestDTO, minus adaptedFrom, plus authorName and the honeypot
public record FriendRecipeRequestDTO(
        @NotBlank(message = "Your name is required")
        @Size(max = 50, message = "Name must be at most 50 characters")
        String authorName,

        // Honeypot: hidden in the form, only bots fill it in
        String website,

        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must be at most 100 characters")
        String title,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        @NotNull(message = "Servings are required")
        @Min(value = 1, message = "Servings must be at least 1")
        @Max(value = 100, message = "Servings must be at most 100")
        Integer servings,

        @PositiveOrZero(message = "Prep time cannot be negative")
        Integer prepTimeMinutes,

        @PositiveOrZero(message = "Cook time cannot be negative")
        Integer cookTimeMinutes,

        @Size(max = 255, message = "Image URL must be at most 255 characters")
        String imageUrl,

        @NotNull(message = "Category is required")
        Long categoryId,

        Set<Long> tagIds,

        @NotEmpty(message = "At least one ingredient is required")
        @Size(max = 30, message = "At most 30 ingredients")
        List<@Valid @NotNull RecipeIngredientRequestDTO> ingredients,

        @NotEmpty(message = "At least one step is required")
        @Size(max = 30, message = "At most 30 steps")
        List<@Valid @NotNull StepRequestDTO> steps
) {

    public boolean isBot() {
        return website != null && !website.isBlank();
    }

    public RecipeRequestDTO toRecipeRequest() {
        return new RecipeRequestDTO(title, description, servings, prepTimeMinutes, cookTimeMinutes,
                imageUrl, null, categoryId, tagIds, ingredients, steps);
    }
}
