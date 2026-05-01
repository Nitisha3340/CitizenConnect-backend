package com.citizenconnect.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.citizenconnect.dto.AnnouncementResponseDTO;
import com.citizenconnect.dto.ApiMessageResponse;
import com.citizenconnect.dto.ComplaintRequestDTO;
import com.citizenconnect.dto.ComplaintResponseDTO;
import com.citizenconnect.dto.IssueRatingRequestDTO;
import com.citizenconnect.entity.Region;
import com.citizenconnect.service.AnnouncementService;
import com.citizenconnect.service.ComplaintService;
import com.citizenconnect.service.IssueRatingService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/citizen")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class CitizenPortalController {

    private final ComplaintService complaintService;
    private final AnnouncementService announcementService;
    private final IssueRatingService issueRatingService;

    public CitizenPortalController(
            ComplaintService complaintService,
            AnnouncementService announcementService,
            IssueRatingService issueRatingService) {
        this.complaintService = complaintService;
        this.announcementService = announcementService;
        this.issueRatingService = issueRatingService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(@RequestHeader("Authorization") String token) {
        return complaintService.getCitizenDashboard(token);
    }

    @GetMapping("/account-stats")
    public Map<String, Object> accountStats(@RequestHeader("Authorization") String token) {
        return complaintService.getCitizenAccountStats(token);
    }

    @PostMapping("/issues")
    public ComplaintResponseDTO raiseIssue(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ComplaintRequestDTO dto) {
        return complaintService.createComplaint(token, dto);
    }

    @GetMapping("/issues")
    public List<ComplaintResponseDTO> myIssues(@RequestHeader("Authorization") String token) {
        return complaintService.getUserComplaints(token);
    }

    @GetMapping("/trending")
    public Map<String, Object> trending(@RequestParam(required = false) Region region) {
        return complaintService.getTrending(region);
    }

    @GetMapping("/announcements")
    public List<AnnouncementResponseDTO> announcements(@RequestHeader("Authorization") String token) {
        return announcementService.listForCitizen(token);
    }

    @PostMapping("/issues/{id}/rating")
    public ApiMessageResponse rateIssue(
            @RequestHeader("Authorization") String token,
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @Valid @RequestBody IssueRatingRequestDTO dto) {
        issueRatingService.rateResolvedIssue(token, id, dto);
        return new ApiMessageResponse("Thanks for your feedback.");
    }
}
