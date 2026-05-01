package com.citizenconnect.dto;

import com.citizenconnect.entity.Region;

import jakarta.validation.constraints.NotBlank;

public class AnnouncementRequestDTO {

    @NotBlank(message = "Content is required")
    private String content;

    private Region region;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }
}
