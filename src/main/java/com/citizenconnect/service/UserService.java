package com.citizenconnect.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.citizenconnect.dto.AuthTokenResponse;
import com.citizenconnect.dto.UserProfileDTO;
import com.citizenconnect.dto.UserRegistrationDTO;
import com.citizenconnect.dto.VerifyOtpRequestDTO;
import com.citizenconnect.entity.Role;
import com.citizenconnect.entity.User;
import com.citizenconnect.repository.UserRepository;
import com.citizenconnect.security.JwtUtil;

@Service
public class UserService {

    private final UserRepository repo;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, OtpService otpService, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(UserRegistrationDTO dto) {
        if (dto.getRole() == Role.ADMIN) {
            throw new RuntimeException("Invalid role for self-registration");
        }
        String emailNorm = dto.getEmail().trim().toLowerCase();
        if (repo.findByEmail(emailNorm).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if (dto.getRole() == Role.CITIZEN && dto.getRegion() == null) {
            throw new RuntimeException("Region is required for citizens");
        }
        if ((dto.getRole() == Role.POLITICIAN || dto.getRole() == Role.MODERATOR) && dto.getRegion() == null) {
            throw new RuntimeException("Region (zone) is required for this role");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(emailNorm);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setRegion(dto.getRegion());
        user.setVerified(true);
        user.setCreatedAt(LocalDateTime.now());
        repo.save(user);
    }

    /** Validates password and sends login OTP to email. */
    public void initiateLogin(String email, String rawPassword) {
        User user = repo.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isBlocked()) {
            throw new RuntimeException("Account is blocked");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        otpService.sendLoginOtp(user.getEmail());
    }

    @Transactional
    public AuthTokenResponse completeLogin(VerifyOtpRequestDTO dto) {
        String email = normalizeEmail(dto.getEmail());
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isBlocked()) {
            throw new RuntimeException("Account is blocked");
        }

        otpService.verifyLoginOtp(email, dto.getOtp());

        String token = JwtUtil.generateToken(user.getEmail());
        return new AuthTokenResponse(token, user.getEmail(), user.getRole());
    }

    public UserProfileDTO updateProfileForUser(String email, UserProfileDTO dto) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getAddress() != null) {
            user.setAddress(dto.getAddress());
        }
        if (dto.getRegion() != null) {
            user.setRegion(dto.getRegion());
        }
        if (dto.getDesignation() != null) {
            user.setDesignation(dto.getDesignation());
        }
        if (dto.getConstituency() != null) {
            user.setConstituency(dto.getConstituency());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }
        if (dto.getProfilePhotoUrl() != null) {
            user.setProfilePhotoUrl(dto.getProfilePhotoUrl());
        }

        repo.save(user);
        return mapToDTO(user);
    }

    private UserProfileDTO mapToDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setRegion(user.getRegion());
        dto.setDesignation(user.getDesignation());
        dto.setConstituency(user.getConstituency());
        dto.setBio(user.getBio());
        dto.setProfilePhotoUrl(user.getProfilePhotoUrl());
        return dto;
    }

    public UserProfileDTO getProfileForUser(String email) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDTO(user);
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
