package com.citizenconnect.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.citizenconnect.dto.UserProfileDTO;
import com.citizenconnect.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get logged-in user profile (email hidden)
     */
    @GetMapping("/profile")
    @Operation(summary = "Get user profile")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserProfileDTO> getProfile() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        UserProfileDTO profile =
                userService.getProfileForUser(auth.getName());

        return ResponseEntity.ok(profile);
    }

    /**
     * Update logged-in user profile using JWT identity
     * Email is NOT exposed or editable
     */
    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @RequestBody UserProfileDTO dto) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        UserProfileDTO updatedProfile =
                userService.updateProfileForUser(
                        auth.getName(),
                        dto);

        return ResponseEntity.ok(updatedProfile);
    }
}