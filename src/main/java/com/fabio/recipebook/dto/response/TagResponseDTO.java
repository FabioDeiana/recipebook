package com.fabio.recipebook.dto.response;

import com.fabio.recipebook.entities.Tag;

public record TagResponseDTO(Long id, String name) {

    public static TagResponseDTO from(Tag tag) {
        return new TagResponseDTO(tag.getId(), tag.getName());
    }
}
