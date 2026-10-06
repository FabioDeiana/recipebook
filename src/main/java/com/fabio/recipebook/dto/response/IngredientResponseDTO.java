package com.fabio.recipebook.dto.response;

import com.fabio.recipebook.entities.Ingredient;

public record IngredientResponseDTO(Long id, String name) {

    public static IngredientResponseDTO from(Ingredient ingredient) {
        return new IngredientResponseDTO(ingredient.getId(), ingredient.getName());
    }
}
