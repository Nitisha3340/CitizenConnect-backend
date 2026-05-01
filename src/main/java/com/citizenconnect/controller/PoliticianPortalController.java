package com.citizenconnect.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.citizenconnect.dto.AnnouncementRequestDTO;
import com.citizenconnect.dto.AnnouncementResponseDTO;
import com.citizenconnect.dto.ComplaintResponseDTO;
import com.citizenconnect.dto.StatusUpdateDTO;
import com.citizenconnect.service.AnnouncementService;
import com.citizenconnect.service.ComplaintService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/politician")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class PoliticianPortalController {

    private final ComplaintService complaintService;
    private final AnnouncementService announcementService;

    public PoliticianPortalController(ComplaintService complaintService, AnnouncementService announcementService) {
        this.complaintService = complaintService;
        this.announcementService = announcementService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(@RequestHeader("Authorization") String token) {
        return complaintService.getPoliticianDashboard(token);
    }

    @GetMapping("/issues")
    public List<ComplaintResponseDTO> issues(@RequestHeader("Authorization") String token) {
        return complaintService.getPoliticianIssues(token);
    }

    @PutMapping("/issues/{id}/status")
    public ComplaintResponseDTO updateStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateDTO dto) {
        return complaintService.updateStatusByPolitician(token, id, dto.getStatus());
    }

    @GetMapping("/analytics")
    public Map<String, Object> analytics(@RequestHeader("Authorization") String token) {
        return complaintService.getPoliticianAnalytics(token);
    }

    @PostMapping("/announcements")
    public AnnouncementResponseDTO publish(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody AnnouncementRequestDTO dto) {
        return announcementService.publish(token, dto);
    }

    @GetMapping("/announcements")
    public List<AnnouncementResponseDTO> listAnnouncements(@RequestHeader("Authorization") String token) {
        return announcementService.listForPolitician(token);
    }
}
