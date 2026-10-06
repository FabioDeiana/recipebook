package com.fabio.recipebook.dto.response;

import com.fabio.recipebook.entities.Step;

public record StepResponseDTO(Integer stepNumber, String description) {

    public static StepResponseDTO from(Step step) {
        return new StepResponseDTO(step.getStepNumber(), step.getDescription());
    }
}
