package com.fabio.recipebook.controllers;

import com.fabio.recipebook.dto.request.FriendRecipeRequestDTO;
import com.fabio.recipebook.services.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/friend-recipes")
@RequiredArgsConstructor
@Slf4j
public class FriendRecipeController {

    private final RecipeService recipeService;

    // Public. No body in the response, so a bot can't tell a honeypot hit from a real save
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void submit(@RequestBody @Valid FriendRecipeRequestDTO request) {
        if (request.isBot()) {
            log.info("Honeypot triggered, friend recipe ignored");
            return;
        }
        recipeService.createFromFriend(request.authorName(), request.toRecipeRequest());
    }
}
