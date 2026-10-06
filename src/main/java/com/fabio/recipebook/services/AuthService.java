package com.fabio.recipebook.services;

import com.fabio.recipebook.dto.request.ChangePasswordRequestDTO;
import com.fabio.recipebook.dto.request.LoginRequestDTO;
import com.fabio.recipebook.dto.response.LoginResponseDTO;
import com.fabio.recipebook.entities.AppUser;
import com.fabio.recipebook.exceptions.BadRequestException;
import com.fabio.recipebook.exceptions.UnauthorizedException;
import com.fabio.recipebook.repositories.AppUserRepository;
import com.fabio.recipebook.security.JwtTools;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTools jwtTools;

    public LoginResponseDTO login(LoginRequestDTO request) {
        // Same message for wrong username and wrong password
        AppUser user = appUserRepository.findByUsername(request.username())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        return new LoginResponseDTO(jwtTools.createToken(user.getUsername()));
    }

    @Transactional
    public void changePassword(AppUser currentUser, ChangePasswordRequestDTO request) {
        AppUser user = appUserRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UnauthorizedException("User no longer exists"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is wrong");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }
}
