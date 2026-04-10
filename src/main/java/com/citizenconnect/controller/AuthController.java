package com.citizenconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.citizenconnect.entity.User;
import com.citizenconnect.service.UserService;
import com.citizenconnect.service.OtpService;
import com.citizenconnect.dto.LoginRequestDTO;
import com.citizenconnect.dto.UserProfileDTO;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private UserService service;

    @Autowired
    private OtpService otpService;

    // 🔹 SIGNUP
    @PostMapping("/signup")
    public String signup(@Valid @RequestBody User user) {
        return service.register(user);
    }

    // 🔹 LOGIN
 // 🔹 LOGIN (UPDATED)
    @PostMapping("/login")
    public String login(@RequestBody LoginRequestDTO request) {
        return service.login(request.getEmail(), request.getPassword());
    }

    // 🔹 SEND OTP
    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam String email) {
        return otpService.generateOtp(email);
    }

    // 🔹 VERIFY OTP
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email, @RequestParam String otp) {

        boolean result = otpService.verifyOtp(email, otp);

        if (result) {
            return "OTP verified successfully";
        }

        return "Invalid OTP";
    }
    
    @GetMapping("/profile")
    public UserProfileDTO getProfile(@RequestHeader("Authorization") String token) {
        return service.getProfile(token);
    }

    @PutMapping("/profile")
    public UserProfileDTO updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UserProfileDTO dto) {
        return service.updateProfile(token, dto);
    }
}