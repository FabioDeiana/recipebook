package com.fabio.recipebook.controllers;

import com.fabio.recipebook.dto.request.ChangePasswordRequestDTO;
import com.fabio.recipebook.dto.request.LoginRequestDTO;
import com.fabio.recipebook.dto.response.LoginResponseDTO;
import com.fabio.recipebook.entities.AppUser;
import com.fabio.recipebook.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody @Valid LoginRequestDTO request) {
        return authService.login(request);
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal AppUser currentUser,
                               @RequestBody @Valid ChangePasswordRequestDTO request) {
        authService.changePassword(currentUser, request);
    }
}
