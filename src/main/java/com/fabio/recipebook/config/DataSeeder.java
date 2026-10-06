package com.fabio.recipebook.config;

import com.fabio.recipebook.entities.AppUser;
import com.fabio.recipebook.entities.Category;
import com.fabio.recipebook.entities.Tag;
import com.fabio.recipebook.repositories.AppUserRepository;
import com.fabio.recipebook.repositories.CategoryRepository;
import com.fabio.recipebook.repositories.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Breakfast", "Starters", "Main Courses", "Side Dishes", "Desserts", "Drinks"
    );

    private static final List<String> DEFAULT_TAGS = List.of(
            "Quick", "Gluten-Free", "Meal Prep"
    );

    private final AppUserRepository appUserRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        seedAdmin();
        seedCategories();
        seedTags();
    }

    private void seedAdmin() {
        if (adminUsername.isBlank() || adminPassword.isBlank()) {
            throw new IllegalStateException("ADMIN_USERNAME and ADMIN_PASSWORD must be set in env.properties");
        }
        if (appUserRepository.existsByUsername(adminUsername)) {
            return;
        }
        // Same rule as PATCH /api/auth/password
        if (adminPassword.length() < 8) {
            throw new IllegalStateException("ADMIN_PASSWORD must be at least 8 characters");
        }
        appUserRepository.save(new AppUser(adminUsername, passwordEncoder.encode(adminPassword), "ADMIN"));
        log.info("Admin user '{}' created", adminUsername);
    }

    private void seedCategories() {
        for (String name : DEFAULT_CATEGORIES) {
            if (!categoryRepository.existsByNameIgnoreCase(name)) {
                categoryRepository.save(new Category(name));
                log.info("Category '{}' created", name);
            }
        }
    }

    private void seedTags() {
        for (String name : DEFAULT_TAGS) {
            if (!tagRepository.existsByNameIgnoreCase(name)) {
                tagRepository.save(new Tag(name));
                log.info("Tag '{}' created", name);
            }
        }
    }
}
