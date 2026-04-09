package com.citizenconnect.dto;


import jakarta.validation.constraints.NotBlank;

import com.citizenconnect.entity.Region;
import com.citizenconnect.entity.Severity;


public class ComplaintRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "Description is required")
    private String description;
    private Region region;
    private Severity severity;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
}