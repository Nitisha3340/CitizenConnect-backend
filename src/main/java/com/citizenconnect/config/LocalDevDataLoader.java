package com.citizenconnect.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.citizenconnect.entity.Region;
import com.citizenconnect.entity.Role;
import com.citizenconnect.entity.User;
import com.citizenconnect.repository.UserRepository;

/**
 * Seeds a default admin only when running with the {@code local} profile (H2).
 * Credentials: admin@local.test / admin123
 */
@Component
@Profile("local")
public class LocalDevDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LocalDevDataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String email = "admin@local.test";
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }
        User admin = new User();
        admin.setName("Local Admin");
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setRegion(Region.NORTH);
        admin.setVerified(true);
        admin.setFlagged(false);
        admin.setBlocked(false);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);
    }
}
