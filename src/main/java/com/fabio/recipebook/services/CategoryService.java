package com.fabio.recipebook.services;

import com.fabio.recipebook.dto.request.CategoryRequestDTO;
import com.fabio.recipebook.dto.response.CategoryResponseDTO;
import com.fabio.recipebook.entities.Category;
import com.fabio.recipebook.exceptions.BadRequestException;
import com.fabio.recipebook.exceptions.NotFoundException;
import com.fabio.recipebook.repositories.CategoryRepository;
import com.fabio.recipebook.repositories.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final RecipeRepository recipeRepository;

    // Sorted by id so the seeded meal order is kept (Breakfast first, Drinks last)
    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findAll(Sort.by("id")).stream()
                .map(CategoryResponseDTO::from)
                .toList();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id " + id + " not found"));
    }

    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Category '" + name + "' already exists");
        }
        return CategoryResponseDTO.from(categoryRepository.save(new Category(name)));
    }

    @Transactional
    public CategoryResponseDTO update(Long id, CategoryRequestDTO request) {
        Category category = findById(id);
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BadRequestException("Category '" + name + "' already exists");
        }
        category.setName(name);
        return CategoryResponseDTO.from(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = findById(id);
        long recipeCount = recipeRepository.countByCategoryId(id);
        if (recipeCount > 0) {
            throw new BadRequestException("Category '" + category.getName() + "' is used by "
                    + recipeCount + " recipe(s); move them to another category first");
        }
        categoryRepository.delete(category);
    }
}
