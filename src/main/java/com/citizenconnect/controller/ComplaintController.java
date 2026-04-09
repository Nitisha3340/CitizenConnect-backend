package com.citizenconnect.controller;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.citizenconnect.dto.ComplaintRequestDTO;
import com.citizenconnect.dto.ComplaintResponseDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
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
        @Valid @RequestBody ComplaintRequestDTO dto
    )
     {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
        return service.createComplaint(email, dto);
    }
    // GET USER COMPLAINTS
    @GetMapping
    public List<ComplaintResponseDTO> getUserComplaints(
            
    ) {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return service.getUserComplaints(email);
    }

    @GetMapping("/trending")
    public java.util.Map<String, Long> getTrending() {
        return service.getTrending();
    }
    
    @PutMapping("/{id}/status")
    public ComplaintResponseDTO updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateDTO dto
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