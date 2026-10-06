package com.fabio.recipebook.services;

import com.fabio.recipebook.dto.request.RecipeIngredientRequestDTO;
import com.fabio.recipebook.dto.request.RecipeRequestDTO;
import com.fabio.recipebook.dto.request.StepRequestDTO;
import com.fabio.recipebook.dto.response.RecipeDetailDTO;
import com.fabio.recipebook.dto.response.RecipeSummaryDTO;
import com.fabio.recipebook.entities.Category;
import com.fabio.recipebook.entities.Recipe;
import com.fabio.recipebook.entities.RecipeIngredient;
import com.fabio.recipebook.entities.Step;
import com.fabio.recipebook.entities.Tag;
import com.fabio.recipebook.enums.Section;
import com.fabio.recipebook.exceptions.BadRequestException;
import com.fabio.recipebook.exceptions.NotFoundException;
import com.fabio.recipebook.repositories.CategoryRepository;
import com.fabio.recipebook.repositories.RecipeRepository;
import com.fabio.recipebook.repositories.RecipeSpecifications;
import com.fabio.recipebook.repositories.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final IngredientService ingredientService;

    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "updatedAt", "title", "lastCookedAt");

    @Transactional(readOnly = true)
    public Page<RecipeSummaryDTO> findAll(String search, Section section, Long categoryId, Long tagId,
                                          Boolean favorite, Pageable pageable) {
        Specification<Recipe> spec = Specification.unrestricted();
        if (search != null && !search.isBlank()) {
            spec = spec.and(RecipeSpecifications.matchesSearch(search));
        }
        if (section != null) {
            spec = spec.and(RecipeSpecifications.hasSection(section));
        }
        if (categoryId != null) {
            spec = spec.and(RecipeSpecifications.hasCategory(categoryId));
        }
        if (tagId != null) {
            spec = spec.and(RecipeSpecifications.hasTag(tagId));
        }
        if (favorite != null) {
            spec = spec.and(RecipeSpecifications.isFavorite(favorite));
        }

        return recipeRepository.findAll(spec, checkSort(pageable)).map(RecipeSummaryDTO::from);
    }

    @Transactional(readOnly = true)
    public RecipeDetailDTO findBySlug(String slug) {
        return recipeRepository.findBySlug(slug)
                .map(RecipeDetailDTO::from)
                .orElseThrow(() -> new NotFoundException("Recipe '" + slug + "' not found"));
    }

    @Transactional
    public RecipeDetailDTO create(RecipeRequestDTO request) {
        Recipe recipe = new Recipe();
        recipe.setSection(Section.OWN);
        applyRequest(recipe, request);
        recipe.setSlug(generateUniqueSlug(recipe.getTitle(), null));

        return RecipeDetailDTO.from(recipeRepository.save(recipe));
    }

    // Public submission: always FRIENDS; categories and tags must already exist (checked in applyRequest)
    @Transactional
    public void createFromFriend(String authorName, RecipeRequestDTO request) {
        Recipe recipe = new Recipe();
        recipe.setSection(Section.FRIENDS);
        recipe.setAuthorName(authorName.trim());
        applyRequest(recipe, request);
        recipe.setSlug(generateUniqueSlug(recipe.getTitle(), null));

        recipeRepository.save(recipe);
    }

    // Section, authorName, favorite and lastCookedAt are not part of the request and stay as they are
    @Transactional
    public RecipeDetailDTO update(Long id, RecipeRequestDTO request) {
        Recipe recipe = findEntityById(id);
        String oldTitle = recipe.getTitle();
        applyRequest(recipe, request);
        if (!recipe.getTitle().equals(oldTitle)) {
            recipe.setSlug(generateUniqueSlug(recipe.getTitle(), recipe.getId()));
        }

        return RecipeDetailDTO.from(recipe);
    }

    @Transactional
    public void delete(Long id) {
        recipeRepository.delete(findEntityById(id));
    }

    @Transactional
    public RecipeDetailDTO toggleFavorite(Long id) {
        Recipe recipe = findEntityById(id);
        recipe.setFavorite(!recipe.isFavorite());
        return RecipeDetailDTO.from(recipe);
    }

    @Transactional
    public RecipeDetailDTO markCooked(Long id) {
        Recipe recipe = findEntityById(id);
        recipe.setLastCookedAt(LocalDate.now());
        return RecipeDetailDTO.from(recipe);
    }

    // Copies a friend's recipe into a new OWN recipe; the original is not changed
    @Transactional
    public RecipeDetailDTO adopt(Long id) {
        Recipe original = findEntityById(id);
        if (original.getSection() != Section.FRIENDS) {
            throw new BadRequestException("Only friends' recipes can be adopted");
        }

        Recipe copy = new Recipe();
        copy.setSection(Section.OWN);
        copy.setTitle(original.getTitle());
        copy.setSlug(generateUniqueSlug(original.getTitle(), null));
        copy.setDescription(original.getDescription());
        copy.setServings(original.getServings());
        copy.setPrepTimeMinutes(original.getPrepTimeMinutes());
        copy.setCookTimeMinutes(original.getCookTimeMinutes());
        copy.setImageUrl(original.getImageUrl());
        copy.setAdaptedFrom("Adapted from " + original.getAuthorName() + "'s recipe");
        copy.setCategory(original.getCategory());
        copy.setTags(new HashSet<>(original.getTags()));

        for (RecipeIngredient item : original.getIngredients()) {
            RecipeIngredient recipeIngredient = new RecipeIngredient();
            recipeIngredient.setRecipe(copy);
            recipeIngredient.setIngredient(item.getIngredient());
            recipeIngredient.setQuantity(item.getQuantity());
            recipeIngredient.setUnit(item.getUnit());
            recipeIngredient.setNote(item.getNote());
            copy.getIngredients().add(recipeIngredient);
        }

        for (Step item : original.getSteps()) {
            Step step = new Step();
            step.setRecipe(copy);
            step.setStepNumber(item.getStepNumber());
            step.setDescription(item.getDescription());
            copy.getSteps().add(step);
        }

        return RecipeDetailDTO.from(recipeRepository.save(copy));
    }

    private Recipe findEntityById(Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recipe with id " + id + " not found"));
    }

    // Copies all editable fields from the request onto the recipe (shared by create and update)
    private void applyRequest(Recipe recipe, RecipeRequestDTO request) {
        recipe.setTitle(request.title().trim());
        recipe.setDescription(trimToNull(request.description()));
        recipe.setServings(request.servings());
        recipe.setPrepTimeMinutes(request.prepTimeMinutes());
        recipe.setCookTimeMinutes(request.cookTimeMinutes());
        recipe.setImageUrl(trimToNull(request.imageUrl()));
        recipe.setAdaptedFrom(trimToNull(request.adaptedFrom()));
        recipe.setCategory(findCategory(request.categoryId()));
        recipe.setTags(findTags(request.tagIds()));

        // clear() + add() instead of a new list, otherwise orphanRemoval breaks
        recipe.getIngredients().clear();
        for (RecipeIngredientRequestDTO item : request.ingredients()) {
            RecipeIngredient recipeIngredient = new RecipeIngredient();
            recipeIngredient.setRecipe(recipe);
            recipeIngredient.setIngredient(ingredientService.findOrCreate(item.name()));
            recipeIngredient.setQuantity(item.quantity());
            recipeIngredient.setUnit(item.unit());
            recipeIngredient.setNote(trimToNull(item.note()));
            recipe.getIngredients().add(recipeIngredient);
        }

        recipe.getSteps().clear();
        List<StepRequestDTO> steps = request.steps();
        for (int i = 0; i < steps.size(); i++) {
            Step step = new Step();
            step.setRecipe(recipe);
            step.setStepNumber(i + 1);
            step.setDescription(steps.get(i).description().trim());
            recipe.getSteps().add(step);
        }
    }

    // Only known fields can be sorted on (an unknown one would be a 500)
    private Pageable checkSort(Pageable pageable) {
        List<Sort.Order> orders = pageable.getSort().stream()
                .map(order -> {
                    if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                        throw new BadRequestException("Cannot sort by '" + order.getProperty()
                                + "'. Allowed: " + SORTABLE_FIELDS);
                    }
                    if (order.getProperty().equals("title")) {
                        return order.ignoreCase();
                    }
                    // "Not cooked in a while": never-cooked recipes come first
                    if (order.getProperty().equals("lastCookedAt") && order.isAscending()) {
                        return order.nullsFirst();
                    }
                    return order;
                })
                .toList();

        // id as tie-breaker, so recipes don't jump between pages when the sorted values are equal
        Sort sort = Sort.by(orders).and(Sort.by("id"));
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BadRequestException("Category with id " + categoryId + " does not exist"));
    }

    private Set<Tag> findTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            Set<Long> missing = new HashSet<>(tagIds);
            tags.forEach(tag -> missing.remove(tag.getId()));
            throw new BadRequestException("Tag(s) with id " + missing + " do not exist");
        }
        return new HashSet<>(tags);
    }

    // "Pasta e Ceci!" -> "pasta-e-ceci"; adds -2, -3... if the slug is already taken.
    // excludeId: the recipe being updated, so it doesn't clash with its own slug
    private String generateUniqueSlug(String title, Long excludeId) {
        String base = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isEmpty()) {
            base = "recipe";
        }

        String slug = base;
        int counter = 2;
        while (slugTaken(slug, excludeId)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }

    private boolean slugTaken(String slug, Long excludeId) {
        return excludeId == null
                ? recipeRepository.existsBySlug(slug)
                : recipeRepository.existsBySlugAndIdNot(slug, excludeId);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
