package com.citizenconnect.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.citizenconnect.dto.IssueRatingRequestDTO;
import com.citizenconnect.entity.Complaint;
import com.citizenconnect.entity.IssueRating;
import com.citizenconnect.entity.IssueStatus;
import com.citizenconnect.entity.Role;
import com.citizenconnect.entity.User;
import com.citizenconnect.repository.ComplaintRepository;
import com.citizenconnect.repository.IssueRatingRepository;
import com.citizenconnect.repository.UserRepository;
import com.citizenconnect.security.JwtUtil;

@Service
public class IssueRatingService {

    private final IssueRatingRepository ratingRepository;
    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public IssueRatingService(
            IssueRatingRepository ratingRepository,
            ComplaintRepository complaintRepository,
            UserRepository userRepository,
            ActivityLogService activityLogService) {
        this.ratingRepository = ratingRepository;
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public void rateResolvedIssue(String token, Long complaintId, IssueRatingRequestDTO dto) {
        String email = JwtUtil.extractEmail(token);
        User citizen = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (citizen.getRole() != Role.CITIZEN) {
            throw new RuntimeException("Only citizens can submit ratings");
        }

        Complaint c = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (!IssueStatus.RESOLVED.name().equals(c.getStatus())) {
            throw new RuntimeException("You can only rate resolved issues");
        }
        if (!c.getUser().getId().equals(citizen.getId())) {
            throw new RuntimeException("You can only rate your own resolved issues");
        }
        if (ratingRepository.existsByComplaint_IdAndCitizen_Id(complaintId, citizen.getId())) {
            throw new RuntimeException("Already rated");
        }

        IssueRating r = new IssueRating();
        r.setComplaint(c);
        r.setCitizen(citizen);
        r.setRating(dto.getRating());
        r.setCreatedAt(LocalDateTime.now());
        ratingRepository.save(r);
        activityLogService.log("Citizen rated resolution for issue #" + complaintId);
    }
}
