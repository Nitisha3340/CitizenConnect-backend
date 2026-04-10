package com.citizenconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.citizenconnect.dto.UserProfileDTO;
import org.springframework.stereotype.Service;

import com.citizenconnect.entity.User;
import com.citizenconnect.repository.UserRepository;
import com.citizenconnect.security.JwtUtil;

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    public String register(User user) {

        if (repo.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        user.setVerified(false); // not verified yet
        repo.save(user);

        return "User registered. Please verify OTP.";
    }
    
    public String login(String email, String password) {

        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }

        if (!user.isVerified()) {
            throw new RuntimeException("Please verify OTP first");
        }

        return JwtUtil.generateToken(user.getEmail());
    }
    public UserProfileDTO updateProfile(String token, UserProfileDTO dto) {

        String email = JwtUtil.extractEmail(token.replace("Bearer ", ""));

        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());

        repo.save(user);

        return mapToDTO(user);
    }
    private UserProfileDTO mapToDTO(User user) {

        UserProfileDTO dto = new UserProfileDTO();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());

        return dto;
    }
    public UserProfileDTO getProfile(String token) {

        String email = JwtUtil.extractEmail(token.replace("Bearer ", ""));

        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToDTO(user);
    }
}