package com.citizenconnect.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.citizenconnect.dto.ApiMessageResponse;
import com.citizenconnect.dto.AuthTokenResponse;
import com.citizenconnect.dto.LoginRequestDTO;
import com.citizenconnect.dto.UserProfileDTO;
import com.citizenconnect.dto.UserRegistrationDTO;
import com.citizenconnect.dto.VerifyOtpRequestDTO;
import com.citizenconnect.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    @Operation(security = {})
    public ApiMessageResponse signup(@Valid @RequestBody UserRegistrationDTO body) {
        userService.register(body);
        return new ApiMessageResponse("Registered. You can log in with email and password (login sends an OTP).");
    }

    /** Step 1: validates password and sends login OTP to email. */
    @PostMapping("/login")
    @Operation(security = {})
    public ApiMessageResponse login(@Valid @RequestBody LoginRequestDTO request) {
        userService.initiateLogin(request.getEmail(), request.getPassword());
        return new ApiMessageResponse("OTP sent to your email. It expires in 10 minutes.");
    }

    /** Step 2: verifies login OTP and returns JWT. */
    @PostMapping("/verify-login")
    @Operation(security = {})
    public AuthTokenResponse verifyLogin(@Valid @RequestBody VerifyOtpRequestDTO body) {
        return userService.completeLogin(body);
    }

    @GetMapping("/profile")
    @SecurityRequirement(name = "bearerAuth")
    public UserProfileDTO getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getProfileForUser(auth.getName());
    }

    @PutMapping("/profile")
    @SecurityRequirement(name = "bearerAuth")
    public UserProfileDTO updateProfile(@RequestBody UserProfileDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.updateProfileForUser(auth.getName(), dto);
    }
}
