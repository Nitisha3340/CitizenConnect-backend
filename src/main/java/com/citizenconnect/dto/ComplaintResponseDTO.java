package com.citizenconnect.dto;

import java.time.LocalDateTime;


import com.citizenconnect.entity.Region;
import com.citizenconnect.entity.Severity;

public class ComplaintResponseDTO {

    private Long id;
    private String title;
    private String description;
    private Region region;
    private Severity severity;
    private String status;
    private String userName;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	 
}