package com.citizenconnect.service;

import java.time.LocalDateTime;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.citizenconnect.entity.Region;
import com.citizenconnect.entity.Severity;
import com.citizenconnect.dto.ComplaintRequestDTO;
import com.citizenconnect.dto.ComplaintResponseDTO;

import com.citizenconnect.entity.Complaint;
import com.citizenconnect.entity.User;
import com.citizenconnect.repository.ComplaintRepository;
import com.citizenconnect.repository.UserRepository;
import com.citizenconnect.security.JwtUtil;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepo;
    

    @Autowired
    private UserRepository userRepo;
    
    private Region region;

    // 📋 GET ALL USER COMPLAINTS (My Issues)
    public List<ComplaintResponseDTO> getUserComplaints(String token) {

        String email = JwtUtil.extractEmail(token.replace("Bearer ", ""));

        List<Complaint> complaints = complaintRepo.findByUser_Email(email);

        List<ComplaintResponseDTO> list = new ArrayList<>();

        for (Complaint c : complaints) {
            list.add(mapToDTO(c));
        }

        return list;
    }
    
    public ComplaintResponseDTO createComplaint(String token, ComplaintRequestDTO dto) {

        String email = JwtUtil.extractEmail(token.replace("Bearer ", ""));

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Complaint complaint = new Complaint();

        complaint.setTitle(dto.getTitle());
        complaint.setDescription(dto.getDescription());
        complaint.setRegion(dto.getRegion() != null ? dto.getRegion() : user.getRegion());
        complaint.setSeverity(dto.getSeverity() != null ? dto.getSeverity() : Severity.MEDIUM);

        complaint.setUser(user);
        complaint.setStatus("PENDING");
        complaint.setCreatedAt(LocalDateTime.now());

        Complaint saved = complaintRepo.save(complaint);

        return mapToDTO(saved);
    }

    // 🔢 TOTAL COUNT (for dashboard)
    public long getTotalComplaints(String email) {
        return complaintRepo.countByUser_Email(email);
    }

    // 📊 DASHBOARD DATA
    public Map<String, Object> getDashboard(String email) {

        Map<String, Object> data = new HashMap<>();

        long total = complaintRepo.countByUser_Email(email);
        long inProgress = complaintRepo.countByUser_EmailAndStatus(email, "IN_PROGRESS");
        long resolved = complaintRepo.countByUser_EmailAndStatus(email, "RESOLVED");

        List<Complaint> recent = complaintRepo
                .findTop5ByUser_EmailOrderByCreatedAtDesc(email);

        data.put("total", total);
        data.put("inProgress", inProgress);
        data.put("resolved", resolved);
        data.put("recent", recent);

        return data;
    }

    // 📈 TRENDING DATA (Severity-based)
    public Map<String, Long> getTrending() {

        Map<String, Long> data = new HashMap<>();
        data.put("LOW", complaintRepo.countBySeverity(Severity.LOW));
        data.put("MEDIUM", complaintRepo.countBySeverity(Severity.MEDIUM));
        data.put("HIGH", complaintRepo.countBySeverity(Severity.HIGH));
        return data;
    }

    // 🧑‍💼 UPDATE STATUS (Admin)
    private ComplaintResponseDTO mapToDTO(Complaint complaint) {

        ComplaintResponseDTO dto = new ComplaintResponseDTO();

        dto.setId(complaint.getId());
        
        dto.setTitle(complaint.getTitle());
        dto.setDescription(complaint.getDescription());
        dto.setRegion(complaint.getRegion());
        dto.setSeverity(complaint.getSeverity());
        dto.setStatus(complaint.getStatus());
        dto.setCreatedAt(complaint.getCreatedAt());
        dto.setUserName(complaint.getUser().getName());

        return dto;
    }
    public List<ComplaintResponseDTO> getAllComplaints() {

        List<Complaint> complaints = complaintRepo.findAll();

        List<ComplaintResponseDTO> list = new ArrayList<>();

        for (Complaint c : complaints) {
            list.add(mapToDTO(c));
        }

        return list;
    }
    public ComplaintResponseDTO updateStatus(Long id, String status) {

        Complaint complaint = complaintRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        complaint.setStatus(status);

        Complaint updated = complaintRepo.save(complaint);

        return mapToDTO(updated);
    }
    public void deleteComplaint(Long id, String token) {

        String email = JwtUtil.extractEmail(token.replace("Bearer ", ""));

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Complaint complaint = complaintRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (!complaint.getRegion().equals(user.getRegion())) {
            throw new RuntimeException("Not allowed to delete this complaint");
        }

        complaintRepo.delete(complaint);
    }
    public List<ComplaintResponseDTO> getComplaintsByRegion(String token) {

        String email = JwtUtil.extractEmail(token.replace("Bearer ", ""));

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Complaint> complaints = complaintRepo.findByRegion(user.getRegion());

        List<ComplaintResponseDTO> list = new ArrayList<>();

        for (Complaint c : complaints) {
            list.add(mapToDTO(c));
        }

        return list;
    }
}