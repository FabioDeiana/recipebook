package com.fabio.recipebook.controllers;

import com.fabio.recipebook.dto.response.IngredientResponseDTO;
import com.fabio.recipebook.services.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping
    public List<IngredientResponseDTO> search(@RequestParam(required = false) String search) {
        return ingredientService.search(search);
    }
}
