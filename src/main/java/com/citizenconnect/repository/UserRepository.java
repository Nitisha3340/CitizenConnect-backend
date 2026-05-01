package com.citizenconnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.citizenconnect.entity.Role;
import com.citizenconnect.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    long countByRole(Role role);

    long countByCreatedAtAfter(java.time.LocalDateTime after);

    long countByFlaggedTrue();

    long countByBlockedTrue();
}