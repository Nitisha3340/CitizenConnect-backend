package com.citizenconnect.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.citizenconnect.dto.ComplaintRequestDTO;
import com.citizenconnect.dto.ComplaintResponseDTO;
import com.citizenconnect.entity.Complaint;
import com.citizenconnect.entity.IssueRating;
import com.citizenconnect.entity.IssueStatus;
import com.citizenconnect.entity.Region;
import com.citizenconnect.entity.Role;
import com.citizenconnect.entity.Severity;
import com.citizenconnect.entity.User;
import com.citizenconnect.repository.ComplaintRepository;
import com.citizenconnect.repository.IssueRatingRepository;
import com.citizenconnect.repository.UserRepository;
import com.citizenconnect.security.JwtUtil;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepo;
    private final UserRepository userRepo;
    private final IssueRatingRepository issueRatingRepository;
    private final ActivityLogService activityLogService;

    public ComplaintService(
            ComplaintRepository complaintRepo,
            UserRepository userRepo,
            IssueRatingRepository issueRatingRepository,
            ActivityLogService activityLogService) {
        this.complaintRepo = complaintRepo;
        this.userRepo = userRepo;
        this.issueRatingRepository = issueRatingRepository;
        this.activityLogService = activityLogService;
    }

    public List<ComplaintResponseDTO> getUserComplaints(String token) {
        String email = JwtUtil.extractEmail(token);
        return complaintRepo.findByUser_Email(email).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComplaintResponseDTO createComplaint(String token, ComplaintRequestDTO dto) {
        String email = JwtUtil.extractEmail(token);
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() != Role.CITIZEN) {
            throw new RuntimeException("Only citizens can raise issues");
        }

        Complaint complaint = new Complaint();
        complaint.setTitle(dto.getTitle());
        String desc = dto.getDescription();
        complaint.setDescription(desc == null || desc.isBlank() ? "" : desc);
        complaint.setRegion(dto.getRegion() != null ? dto.getRegion() : user.getRegion());
        if (complaint.getRegion() == null) {
            throw new RuntimeException("Region is required to raise an issue");
        }
        complaint.setSeverity(dto.getSeverity() != null ? dto.getSeverity() : Severity.MEDIUM);
        complaint.setUser(user);
        complaint.setStatus(IssueStatus.PENDING.name());
        complaint.setCreatedAt(LocalDateTime.now());

        Complaint saved = complaintRepo.save(complaint);
        activityLogService.log("New issue #" + saved.getId() + " raised in " + saved.getRegion());
        return mapToDTO(saved);
    }

    public Map<String, Object> getCitizenAccountStats(String token) {
        String email = JwtUtil.extractEmail(token);
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Map<String, Object> m = new HashMap<>();
        m.put("totalIssuesRaised", complaintRepo.countByUser_Email(email));
        m.put("issuesResolved", complaintRepo.countByUser_EmailAndStatus(email, IssueStatus.RESOLVED.name()));
        m.put("memberSince", user.getCreatedAt());
        return m;
    }

    public Map<String, Object> getCitizenDashboard(String token) {
        String email = JwtUtil.extractEmail(token);
        Map<String, Object> data = new HashMap<>();
        long total = complaintRepo.countByUser_Email(email);
        long inProgress = complaintRepo.countByUser_EmailAndStatus(email, IssueStatus.IN_PROGRESS.name());
        long resolved = complaintRepo.countByUser_EmailAndStatus(email, IssueStatus.RESOLVED.name());
        List<ComplaintResponseDTO> recent = complaintRepo.findTop5ByUser_EmailOrderByCreatedAtDesc(email).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        data.put("totalIssues", total);
        data.put("inProgress", inProgress);
        data.put("resolved", resolved);
        data.put("recentIssues", recent);
        return data;
    }

    public Map<String, Object> getTrending(Region regionFilter) {
        Map<String, Object> data = new HashMap<>();
        if (regionFilter == null) {
            data.put("low", complaintRepo.countBySeverity(Severity.LOW));
            data.put("medium", complaintRepo.countBySeverity(Severity.MEDIUM));
            data.put("high", complaintRepo.countBySeverity(Severity.HIGH));
            List<Complaint> high = complaintRepo.findTop3BySeverityOrderByCreatedAtDesc(Severity.HIGH);
            data.put("highSeverityIssues", high.stream().map(this::mapToDTO).collect(Collectors.toList()));
            return data;
        }
        data.put("low", complaintRepo.countByRegionAndSeverity(regionFilter, Severity.LOW));
        data.put("medium", complaintRepo.countByRegionAndSeverity(regionFilter, Severity.MEDIUM));
        data.put("high", complaintRepo.countByRegionAndSeverity(regionFilter, Severity.HIGH));
        List<Complaint> highList =
                complaintRepo.findTop3ByRegionAndSeverityOrderByCreatedAtDesc(regionFilter, Severity.HIGH);
        data.put("highSeverityIssues", highList.stream().map(this::mapToDTO).collect(Collectors.toList()));
        return data;
    }

    public List<ComplaintResponseDTO> getPoliticianIssues(String token) {
        User user = requirePolitician(token);
        Region region = user.getRegion();
        if (region == null) {
            return List.of();
        }
        return complaintRepo.findByRegionOrderByCreatedAtDesc(region).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getPoliticianDashboard(String token) {
        User user = requirePolitician(token);
        Region region = user.getRegion();
        Map<String, Object> m = new HashMap<>();
        if (region == null) {
            m.put("totalIssues", 0);
            m.put("resolved", 0);
            m.put("pending", 0);
            m.put("recentHighPriority", List.of());
            return m;
        }
        long total = complaintRepo.countByRegion(region);
        long resolved = complaintRepo.countByRegionAndStatus(region, IssueStatus.RESOLVED.name());
        long pending = complaintRepo.countByRegionAndStatus(region, IssueStatus.PENDING.name())
                + complaintRepo.countByRegionAndStatus(region, IssueStatus.IN_PROGRESS.name());
        List<ComplaintResponseDTO> recentHigh = complaintRepo
                .findTop3ByRegionAndSeverityOrderByCreatedAtDesc(region, Severity.HIGH).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        m.put("totalIssues", total);
        m.put("resolved", resolved);
        m.put("pending", pending);
        m.put("recentHighPriority", recentHigh);
        return m;
    }

    public Map<String, Object> getPoliticianAnalytics(String token) {
        User user = requirePolitician(token);
        Region region = user.getRegion();
        Map<String, Object> out = new HashMap<>();
        List<Map<String, Object>> solvedPerMonth = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 3; i >= 0; i--) {
            YearMonth ym = YearMonth.from(today.minusMonths(i));
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();
            long count = region == null ? 0
                    : complaintRepo.countByRegionAndStatusAndResolvedAtBetween(
                            region, IssueStatus.RESOLVED.name(), start, end);
            Map<String, Object> row = new HashMap<>();
            row.put("label", ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            row.put("count", count);
            solvedPerMonth.add(row);
        }
        out.put("issuesSolvedPerMonth", solvedPerMonth);

        LocalDateTime since = today.minusMonths(4).withDayOfMonth(1).atStartOfDay();
        List<Map<String, Object>> ratingPerMonth = new ArrayList<>();
        if (region != null) {
            List<IssueRating> ratings = issueRatingRepository.findByComplaintRegionSince(region, since);
            for (int i = 3; i >= 0; i--) {
                YearMonth ym = YearMonth.from(today.minusMonths(i));
                LocalDateTime start = ym.atDay(1).atStartOfDay();
                LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();
                List<Integer> monthRatings = ratings.stream()
                        .filter(r -> !r.getCreatedAt().isBefore(start) && r.getCreatedAt().isBefore(end))
                        .map(IssueRating::getRating)
                        .collect(Collectors.toList());
                double avg = monthRatings.isEmpty() ? 0.0
                        : monthRatings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                Map<String, Object> row = new HashMap<>();
                row.put("label", ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
                row.put("averageRating", Math.round(avg * 10.0) / 10.0);
                ratingPerMonth.add(row);
            }
        }
        out.put("surveyRatingPerMonth", ratingPerMonth);
        return out;
    }

    @Transactional
    public ComplaintResponseDTO updateStatusByPolitician(String token, Long id, String status) {
        User politician = requirePolitician(token);
        Complaint complaint = complaintRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        if (politician.getRegion() == null || !complaint.getRegion().equals(politician.getRegion())) {
            throw new RuntimeException("You can only update issues in your zone");
        }

        IssueStatus parsed = parseStatus(status);
        complaint.setStatus(parsed.name());
        if (parsed == IssueStatus.RESOLVED) {
            complaint.setResolvedAt(LocalDateTime.now());
        } else {
            complaint.setResolvedAt(null);
        }
        Complaint updated = complaintRepo.save(complaint);
        activityLogService.log(
                "Politician updated issue #" + id + " to " + parsed.name() + " (" + complaint.getRegion() + ")");
        return mapToDTO(updated);
    }

    public List<ComplaintResponseDTO> getAllForModerator() {
        return complaintRepo.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void deleteByModerator(String token, Long id) {
        User mod = requireModerator(token);
        Complaint complaint = complaintRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        complaintRepo.delete(complaint);
        activityLogService.log("Moderator " + mod.getEmail() + " deleted issue #" + id);
    }

    private User requirePolitician(String token) {
        String email = JwtUtil.extractEmail(token);
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() != Role.POLITICIAN) {
            throw new RuntimeException("Politician access only");
        }
        return user;
    }

    private User requireModerator(String token) {
        String email = JwtUtil.extractEmail(token);
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() != Role.MODERATOR) {
            throw new RuntimeException("Moderator access only");
        }
        return user;
    }

    private static IssueStatus parseStatus(String status) {
        if (status == null) {
            throw new RuntimeException("Status is required");
        }
        try {
            return IssueStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid status");
        }
    }

    private ComplaintResponseDTO mapToDTO(Complaint complaint) {
        ComplaintResponseDTO dto = new ComplaintResponseDTO();
        dto.setId(complaint.getId());
        dto.setTitle(complaint.getTitle());
        dto.setDescription(complaint.getDescription());
        dto.setRegion(complaint.getRegion());
        dto.setSeverity(complaint.getSeverity());
        dto.setStatus(complaint.getStatus());
        dto.setStatusLabel(toStatusLabel(complaint.getStatus()));
        dto.setCreatedAt(complaint.getCreatedAt());
        dto.setResolvedAt(complaint.getResolvedAt());
        dto.setUserName(complaint.getUser() != null ? complaint.getUser().getName() : null);
        return dto;
    }

    private static String toStatusLabel(String status) {
        if (IssueStatus.IN_PROGRESS.name().equals(status)) {
            return "In Progress";
        }
        if (IssueStatus.RESOLVED.name().equals(status)) {
            return "Resolved";
        }
        if (IssueStatus.PENDING.name().equals(status)) {
            return "Pending";
        }
        return status;
    }
}
