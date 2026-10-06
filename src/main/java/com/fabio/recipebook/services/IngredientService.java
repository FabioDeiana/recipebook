package com.fabio.recipebook.services;

import com.fabio.recipebook.dto.response.IngredientResponseDTO;
import com.fabio.recipebook.entities.Ingredient;
import com.fabio.recipebook.repositories.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    // Autocomplete: max 10 results, empty list when nothing has been typed yet
    public List<IngredientResponseDTO> search(String search) {
        if (search == null || search.isBlank()) {
            return List.of();
        }
        return ingredientRepository.findTop10ByNameContainingIgnoreCaseOrderByNameAsc(search.trim()).stream()
                .map(IngredientResponseDTO::from)
                .toList();
    }

    // Used when saving a recipe: existing ingredient matched case-insensitively, otherwise created
    @Transactional
    public Ingredient findOrCreate(String name) {
        String normalized = name.trim().toLowerCase();
        return ingredientRepository.findByNameIgnoreCase(normalized)
                .orElseGet(() -> ingredientRepository.save(new Ingredient(normalized)));
    }
}
