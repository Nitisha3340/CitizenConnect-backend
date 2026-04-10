package com.citizenconnect.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.citizenconnect.dto.ComplaintRequestDTO;
import com.citizenconnect.dto.ComplaintResponseDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.citizenconnect.dto.StatusUpdateDTO;

import com.citizenconnect.service.ComplaintService;

@RestController
@RequestMapping("/complaints")
@CrossOrigin("*")
public class ComplaintController {

    @Autowired
    private ComplaintService service;

    // ✅ CREATE
    @PostMapping
    public ComplaintResponseDTO create(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ComplaintRequestDTO dto) {

        return service.createComplaint(token, dto);
    }

    // ✅ MY ISSUES
    @GetMapping("/my")
    public List<ComplaintResponseDTO> getUserComplaints(
            @RequestHeader("Authorization") String token) {

        return service.getUserComplaints(token);
    }

    // ✅ ALL (Admin / Politician)
    @GetMapping
    public List<ComplaintResponseDTO> getAll() {
        return service.getAllComplaints();
    }

    // ✅ TRENDING
    @GetMapping("/trending")
    public Map<String, Long> getTrending() {
        return service.getTrending();
    }

    // ✅ UPDATE STATUS
    @PutMapping("/{id}/status")
    public ComplaintResponseDTO updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateDTO dto) {

        return service.updateStatus(id, dto.getStatus());
    }

    // ✅ DELETE (SECURE)
    @DeleteMapping("/{id}")
    public void deleteComplaint(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        service.deleteComplaint(id, token);
    }

    // ✅ REGION FILTER
    @GetMapping("/region")
    public List<ComplaintResponseDTO> getByRegion(
            @RequestHeader("Authorization") String token) {

        return service.getComplaintsByRegion(token);
    }
}