package com.citizenconnect.controller;

import java.util.List;
import com.citizenconnect.dto.ComplaintRequestDTO;
import com.citizenconnect.dto.ComplaintResponseDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.citizenconnect.dto.StatusUpdateDTO;

import com.citizenconnect.entity.Complaint;
import com.citizenconnect.security.JwtUtil;
import com.citizenconnect.service.ComplaintService;

@RestController
@RequestMapping("/complaints")
@CrossOrigin("*")
public class ComplaintController {

    @Autowired
    private ComplaintService service;

    // CREATE
    @PostMapping
    public ComplaintResponseDTO create(
        @RequestHeader("Authorization") String token,
        @RequestBody ComplaintRequestDTO dto
    ) {
        String email = JwtUtil.extractEmail(token.replace("Bearer ", ""));
        return service.createComplaint(email, dto);
    }
    // GET USER COMPLAINTS
    @GetMapping
    public List<ComplaintResponseDTO> getUserComplaints(
            @RequestHeader("Authorization") String token
    ) {
        String email = JwtUtil.extractEmail(token.replace("Bearer ", ""));
        return service.getUserComplaints(email);
    }
    
    @PutMapping("/{id}/status")
    public ComplaintResponseDTO updateStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateDTO dto
    ) {
        return service.updateStatus(id, dto.getStatus());
    }
    @DeleteMapping("/{id}")
    public String deleteComplaint(@PathVariable Long id,
                                 @RequestParam String email) {
        service.deleteComplaint(id, email);
        return "Deleted successfully";
    }
    @GetMapping("/region")
    public List<ComplaintResponseDTO> getByRegion(@RequestParam String email) {
        return service.getComplaintsByRegion(email);
    }
}