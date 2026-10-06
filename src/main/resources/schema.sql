-- Runs at every startup, before Hibernate updates the tables.
-- unaccent makes the recipe search accent-insensitive ("tiramisu" finds "Tiramisù").
CREATE EXTENSION IF NOT EXISTS unaccent;
