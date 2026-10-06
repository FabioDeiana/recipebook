package com.fabio.recipebook.repositories;

import com.fabio.recipebook.entities.Ingredient;
import com.fabio.recipebook.entities.Recipe;
import com.fabio.recipebook.entities.RecipeIngredient;
import com.fabio.recipebook.entities.Tag;
import com.fabio.recipebook.enums.Section;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

// Filters for GET /api/recipes, combined in RecipeService.
// EXISTS subqueries instead of joins: no duplicate rows, so no DISTINCT needed and paging stays correct.
public final class RecipeSpecifications {

    private static final char ESCAPE = '\\';

    private RecipeSpecifications() {
    }

    // Case- and accent-insensitive match on title, description, ingredient names or tag names
    // ("tiramisu" finds "Tiramisù" and vice versa; needs the unaccent extension, see schema.sql)
    public static Specification<Recipe> matchesSearch(String search) {
        String rawPattern = "%" + escapeLike(search.trim()) + "%";

        return (root, query, cb) -> {
            Expression<String> pattern = normalize(cb, cb.literal(rawPattern));

            Subquery<Long> ingredientMatch = query.subquery(Long.class);
            Root<RecipeIngredient> recipeIngredient = ingredientMatch.from(RecipeIngredient.class);
            Join<RecipeIngredient, Ingredient> ingredient = recipeIngredient.join("ingredient");
            ingredientMatch.select(recipeIngredient.get("id")).where(
                    cb.equal(recipeIngredient.get("recipe"), root),
                    cb.like(normalize(cb, ingredient.get("name")), pattern, ESCAPE)
            );

            Subquery<Long> tagMatch = query.subquery(Long.class);
            Root<Recipe> tagged = tagMatch.from(Recipe.class);
            Join<Recipe, Tag> tag = tagged.join("tags");
            tagMatch.select(tagged.get("id")).where(
                    cb.equal(tagged, root),
                    cb.like(normalize(cb, tag.get("name")), pattern, ESCAPE)
            );

            return cb.or(
                    cb.like(normalize(cb, root.get("title")), pattern, ESCAPE),
                    cb.like(normalize(cb, root.get("description")), pattern, ESCAPE),
                    cb.exists(ingredientMatch),
                    cb.exists(tagMatch)
            );
        };
    }

    // lower(unaccent(value)), applied to both the column and the search text
    private static Expression<String> normalize(CriteriaBuilder cb, Expression<String> value) {
        return cb.lower(cb.function("unaccent", String.class, value));
    }

    public static Specification<Recipe> hasSection(Section section) {
        return (root, query, cb) -> cb.equal(root.get("section"), section);
    }

    public static Specification<Recipe> hasCategory(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Recipe> hasTag(Long tagId) {
        return (root, query, cb) -> {
            Subquery<Long> tagMatch = query.subquery(Long.class);
            Root<Recipe> tagged = tagMatch.from(Recipe.class);
            Join<Recipe, Tag> tag = tagged.join("tags");
            tagMatch.select(tagged.get("id")).where(
                    cb.equal(tagged, root),
                    cb.equal(tag.get("id"), tagId)
            );
            return cb.exists(tagMatch);
        };
    }

    public static Specification<Recipe> isFavorite(boolean favorite) {
        return (root, query, cb) -> cb.equal(root.get("favorite"), favorite);
    }

    // So that "%" or "_" typed by the user are searched literally
    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
