package com.fabio.recipebook.dto.request;

import com.fabio.recipebook.enums.Unit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RecipeIngredientRequestDTO(
        // Matched case-insensitively; created on the fly if it doesn't exist
        @NotBlank(message = "Ingredient name is required")
        @Size(max = 100, message = "Ingredient name must be at most 100 characters")
        String name,

        // Null allowed, e.g. for TO_TASTE
        @Positive(message = "Quantity must be greater than 0")
        Double quantity,

        @NotNull(message = "Unit is required")
        Unit unit,

        @Size(max = 255, message = "Note must be at most 255 characters")
        String note
) {
}
