package com.citizenconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class StatusUpdateDTO {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PENDING|IN_PROGRESS|RESOLVED", message = "Status must be one of: PENDING, IN_PROGRESS, RESOLVED")
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}