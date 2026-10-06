# RecipeBook — Backend

A personal recipe website, built as a surprise gift. One owner (admin) manages her recipes; friends can browse without an account and submit their own recipes to a separate "Friends' Recipes" section.

## How to work with me

- Reply in **Italian**. All code, identifiers, UI text and API messages are in **English**.
- Keep answers short and step by step. One step at a time; wait for me before moving on.
- When a file changes, give or write the **complete file**, not fragments.
- Don't add features or dependencies I haven't asked for.

## Stack

- Java 21, Spring Boot 4.1.1, Maven
- Spring Web MVC, Spring Data JPA (Hibernate 7), PostgreSQL, Spring Security, Validation, Lombok
- JJWT 0.12.6 for JWT auth
- Frontend (separate project, later): React + Vite, runs on http://localhost:5173

## Configuration

- Secrets live in `env.properties` in the project root, imported with `spring.config.import=file:env.properties`. This file is in `.gitignore` — never commit it, never hardcode secrets.
- Keys in `env.properties`: `PORT`, `PG_DB_NAME`, `PG_USERNAME`, `PG_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`.
- Inject values with `@Value("${...}")`.
- Local DB: PostgreSQL on localhost:5432, database `recipebook`, `ddl-auto=update`.

## Package structure

```
com.fabio.recipebook
├── config          SecurityConfig, CorsConfig, DataSeeder
├── controllers
├── dto/request
├── dto/response
├── entities
├── enums           Section, Unit
├── exceptions      NotFoundException, BadRequestException, UnauthorizedException, GlobalExceptionHandler
├── repositories
├── security        JwtTools, JwtFilter, RateLimitFilter
└── services
```

## Data model

All recipes are vegetarian or vegan — no diet flags. No difficulty field.

**AppUser** — `id`, `username`, `password` (BCrypt), `role`. Only one user: the admin. No registration; she is created at startup by `DataSeeder` from `ADMIN_USERNAME` / `ADMIN_PASSWORD` if she doesn't exist. After that, she changes her password via `PATCH /api/auth/password` (changing `ADMIN_PASSWORD` later has no effect).

**Recipe** — `id`, `title`, `slug` (unique, generated from title), `description`, `servings`, `prepTimeMinutes`, `cookTimeMinutes`, `favorite` (boolean), `imageUrl`, `lastCookedAt` (LocalDate, nullable), `section` (enum Section: OWN / FRIENDS), `authorName` (friend's name, null for OWN), `adaptedFrom` (e.g. "Adapted from Giulia's recipe", nullable), `createdAt`, `updatedAt`.
- `@ManyToOne` Category
- `@ManyToMany` Tag
- `@OneToMany` RecipeIngredient (cascade all, orphanRemoval)
- `@OneToMany` Step (cascade all, orphanRemoval, ordered by stepNumber)
- No relation to AppUser.

**Category** — `id`, `name` (unique). Seeded: Breakfast, Starters, Main Courses, Side Dishes, Desserts, Drinks.

**Tag** — `id`, `name` (unique). Seeded: Quick, Gluten-Free, Meal Prep.

**Ingredient** — `id`, `name` (unique, stored lowercase). Separate table so recipes can be searched by ingredient.

**RecipeIngredient** — `id`, `quantity` (Double, nullable), `unit` (enum Unit — metric: G, KG, ML, CL, DL, L; US: OZ, LB, FL_OZ, CUP, PINT, QUART; common: TSP, TBSP, PIECE, PINCH, TO_TASTE), `note` (e.g. "finely chopped"); `@ManyToOne` Recipe, `@ManyToOne` Ingredient.

**Step** — `id`, `stepNumber`, `description`; `@ManyToOne` Recipe.

Serving scaling is done in the frontend (`quantity * newServings / servings`, TO_TASTE unchanged) — nothing in the DB.

## Security

- `POST /api/auth/login` → returns JWT. Token expiration from `JWT_EXPIRATION` (7 days).
- All `GET /api/**` are public. Everything else requires a valid JWT, except `POST /api/friend-recipes` (public).
- Stateless sessions, CSRF disabled, CORS allows the frontend origin.
- Never serialize entities directly: use DTOs (bidirectional relations would cause infinite JSON loops).

## Endpoints

**Auth**
- `POST /api/auth/login` — public; wrong credentials → 401
- `PATCH /api/auth/password` — requires JWT; body `currentPassword` + `newPassword` (8–72 chars); wrong current password → 400; returns 204

**Recipes**
- `GET /api/recipes?search=&section=&categoryId=&tagId=&favorite=&page=&size=&sort=` — list with search and filters, returns `RecipeSummaryDTO` page
- `GET /api/recipes/{slug}` — full `RecipeDetailDTO`
- `POST /api/recipes` — create full recipe (ingredients + steps in one request, section always OWN)
- `PUT /api/recipes/{id}` — replace full recipe; keeps section, authorName, favorite, lastCookedAt; slug is regenerated only if the title changes
- `DELETE /api/recipes/{id}` — works for both OWN and FRIENDS recipes
- `PATCH /api/recipes/{id}/favorite` — toggle favorite
- `PATCH /api/recipes/{id}/cooked` — set `lastCookedAt` to today
- `POST /api/recipes/{id}/adopt` — copy a FRIENDS recipe into a new OWN recipe with `adaptedFrom` set; the original stays untouched

**Friend submissions**
- `POST /api/friend-recipes` — public; always forces `section = FRIENDS`; `authorName` required; can only use existing categories and tags

**Categories** — `GET`, `POST`, `PUT /{id}`, `DELETE /{id}` on `/api/categories`. Names unique (case-insensitive). Deleting a category used by recipes → 400.
**Tags** — `GET`, `POST`, `DELETE /{id}` on `/api/tags`. Names unique (case-insensitive). Deleting a tag removes it from all recipes (recipes are kept).
**Ingredients** — `GET /api/ingredients?search=` for form autocomplete

When creating or updating a recipe, ingredients are matched by name (case-insensitive); if one doesn't exist it's created on the fly.

Useful sorts: `createdAt,desc`, `title,asc`, `lastCookedAt,asc` ("not cooked in a while", never-cooked first). Only `createdAt`, `updatedAt`, `title`, `lastCookedAt` are sortable (others → 400). Default page size 12, max 50. Page JSON: `{ content, page: { size, number, totalElements, totalPages } }`.

## Search

Keyword search is case-insensitive across title, description, ingredient names and tag names, combined with the filters via Spring Data JPA Specifications (`RecipeSpecifications`, EXISTS subqueries — no DISTINCT needed). Also accent-insensitive via PostgreSQL `unaccent` ("tiramisu" finds "Tiramisù"); the extension is enabled at startup by `schema.sql` (`spring.sql.init.mode=always`), so it also works on the online DB.

## Friend submission protection

- **Honeypot**: `FriendRecipeRequestDTO` has a hidden `website` field. If it's not empty, return 201 without saving. The response has no body in both cases, so bots can't tell the difference.
- **Rate limit**: max 5 submissions per hour per IP on `POST /api/friend-recipes`, return 429 with `Retry-After` when exceeded (Bucket4j 8.21, `RateLimitFilter`, in memory — resets on restart).
- **Validation**: `authorName` max 50, `title` max 100, `description` max 2000, max 30 ingredients, max 30 steps.

## Later (not now)

- Image upload with Cloudinary (for now `imageUrl` is a plain string)
- Optional AI recipe import with the Anthropic Java SDK
- Deploy (Koyeb or similar, online PostgreSQL)
