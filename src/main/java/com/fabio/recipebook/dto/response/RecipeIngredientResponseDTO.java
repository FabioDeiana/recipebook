package com.fabio.recipebook.dto.response;

import com.fabio.recipebook.entities.RecipeIngredient;
import com.fabio.recipebook.enums.Unit;

public record RecipeIngredientResponseDTO(Long id, String name, Double quantity, Unit unit, String note) {

    public static RecipeIngredientResponseDTO from(RecipeIngredient recipeIngredient) {
        return new RecipeIngredientResponseDTO(
                recipeIngredient.getId(),
                recipeIngredient.getIngredient().getName(),
                recipeIngredient.getQuantity(),
                recipeIngredient.getUnit(),
                recipeIngredient.getNote()
        );
    }
}
