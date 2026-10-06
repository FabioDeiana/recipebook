package com.fabio.recipebook.controllers;

import com.fabio.recipebook.dto.request.CategoryRequestDTO;
import com.fabio.recipebook.dto.response.CategoryResponseDTO;
import com.fabio.recipebook.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponseDTO> findAll() {
        return categoryService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDTO create(@RequestBody @Valid CategoryRequestDTO request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    public CategoryResponseDTO update(@PathVariable Long id, @RequestBody @Valid CategoryRequestDTO request) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
