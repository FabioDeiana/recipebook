package com.fabio.recipebook.controllers;

import com.fabio.recipebook.dto.request.RecipeRequestDTO;
import com.fabio.recipebook.dto.response.RecipeDetailDTO;
import com.fabio.recipebook.dto.response.RecipeSummaryDTO;
import com.fabio.recipebook.enums.Section;
import com.fabio.recipebook.services.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping
    public Page<RecipeSummaryDTO> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Section section,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) Boolean favorite,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return recipeService.findAll(search, section, categoryId, tagId, favorite, pageable);
    }

    @GetMapping("/{slug}")
    public RecipeDetailDTO findBySlug(@PathVariable String slug) {
        return recipeService.findBySlug(slug);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecipeDetailDTO create(@RequestBody @Valid RecipeRequestDTO request) {
        return recipeService.create(request);
    }

    @PutMapping("/{id}")
    public RecipeDetailDTO update(@PathVariable Long id, @RequestBody @Valid RecipeRequestDTO request) {
        return recipeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        recipeService.delete(id);
    }

    @PatchMapping("/{id}/favorite")
    public RecipeDetailDTO toggleFavorite(@PathVariable Long id) {
        return recipeService.toggleFavorite(id);
    }

    @PatchMapping("/{id}/cooked")
    public RecipeDetailDTO markCooked(@PathVariable Long id) {
        return recipeService.markCooked(id);
    }

    @PostMapping("/{id}/adopt")
    @ResponseStatus(HttpStatus.CREATED)
    public RecipeDetailDTO adopt(@PathVariable Long id) {
        return recipeService.adopt(id);
    }
}
