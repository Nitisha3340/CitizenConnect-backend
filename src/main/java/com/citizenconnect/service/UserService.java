package com.citizenconnect.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.citizenconnect.dto.UserProfileDTO;
import com.citizenconnect.dto.UserRegistrationDTO;
import com.citizenconnect.entity.Role;
import com.citizenconnect.entity.User;
import com.citizenconnect.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(UserRegistrationDTO dto) {

        // Prevent admin self-registration
        if (dto.getRole() == Role.ADMIN) {
            throw new RuntimeException("Invalid role for self-registration");
        }

        String emailNorm = dto.getEmail().trim().toLowerCase();

        // Check duplicate email
        if (repo.findByEmail(emailNorm).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Region validation
        if (dto.getRole() == Role.CITIZEN && dto.getRegion() == null) {
            throw new RuntimeException("Region is required for citizens");
        }

        if ((dto.getRole() == Role.POLITICIAN || dto.getRole() == Role.MODERATOR)
                && dto.getRegion() == null) {
            throw new RuntimeException("Region (zone) is required for this role");
        }

        // Create new user
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

    @Transactional
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

        // Email intentionally removed
        // dto.setEmail(user.getEmail());

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
}