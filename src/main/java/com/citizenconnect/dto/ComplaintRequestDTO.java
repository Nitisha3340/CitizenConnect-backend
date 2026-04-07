package com.citizenconnect.dto;

public class ComplaintRequestDTO {

    private String title;
    private String description;
    private String region;
    private String severity;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}