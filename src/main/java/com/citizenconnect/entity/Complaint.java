package com.citizenconnect.entity;

import jakarta.persistence.*;
import com.citizenconnect.entity.Region;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    private String status; // PENDING, IN_PROGRESS, RESOLVED

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    private Region region;     // North, South etc.
    private String severity;   // LOW, MEDIUM, HIGH

    // getters & setters

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}