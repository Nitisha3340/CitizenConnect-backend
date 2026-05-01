package com.citizenconnect.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.citizenconnect.dto.ComplaintResponseDTO;
import com.citizenconnect.service.ComplaintService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/moderator")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class ModeratorPortalController {

    private final ComplaintService complaintService;

    public ModeratorPortalController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping("/issues")
    public List<ComplaintResponseDTO> allIssues() {
        return complaintService.getAllForModerator();
    }

    @DeleteMapping("/issues/{id}")
    public void delete(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        complaintService.deleteByModerator(token, id);
    }
}
