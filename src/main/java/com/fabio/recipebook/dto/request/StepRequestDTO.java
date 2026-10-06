package com.fabio.recipebook.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// No stepNumber: steps are numbered by their position in the list
public record StepRequestDTO(
        @NotBlank(message = "Step description is required")
        @Size(max = 2000, message = "Step description must be at most 2000 characters")
        String description
) {
}
