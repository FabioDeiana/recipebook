package com.fabio.recipebook.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(max = 50, message = "Name must be at most 50 characters")
        String name
) {
}
