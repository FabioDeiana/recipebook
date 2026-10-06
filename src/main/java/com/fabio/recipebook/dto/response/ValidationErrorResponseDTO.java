package com.fabio.recipebook.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ValidationErrorResponseDTO(String message, Map<String, String> errors, LocalDateTime timestamp) {
}
