package com.fabio.recipebook.dto.response;

import com.fabio.recipebook.entities.Category;

public record CategoryResponseDTO(Long id, String name) {

    public static CategoryResponseDTO from(Category category) {
        return new CategoryResponseDTO(category.getId(), category.getName());
    }
}
