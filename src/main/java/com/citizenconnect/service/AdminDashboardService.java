package com.citizenconnect.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.citizenconnect.entity.Role;
import com.citizenconnect.entity.IssueStatus;
import com.citizenconnect.repository.ActivityLogRepository;
import com.citizenconnect.repository.ComplaintRepository;
import com.citizenconnect.repository.UserRepository;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;
    private final ActivityLogRepository activityLogRepository;

    public AdminDashboardService(
            UserRepository userRepository,
            ComplaintRepository complaintRepository,
            ActivityLogRepository activityLogRepository) {
        this.userRepository = userRepository;
        this.complaintRepository = complaintRepository;
        this.activityLogRepository = activityLogRepository;
    }

    public Map<String, Object> overview() {
        Map<String, Object> m = new HashMap<>();
        long totalUsers = userRepository.count();
        long citizens = userRepository.countByRole(Role.CITIZEN);
        long moderators = userRepository.countByRole(Role.MODERATOR);
        long politicians = userRepository.countByRole(Role.POLITICIAN);
        long totalIssues = complaintRepository.count();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long newRegistrationsToday = userRepository.countByCreatedAtAfter(startOfDay);

        m.put("totalUsers", totalUsers);
        m.put("activeCitizens", citizens);
        m.put("moderators", moderators);
        m.put("politicians", politicians);
        m.put("totalIssues", totalIssues);
        m.put("newRegistrationsToday", newRegistrationsToday);
        m.put("flaggedAccounts", userRepository.countByFlaggedTrue());
        m.put("blockedUsers", userRepository.countByBlockedTrue());
        m.put("pendingIssues", complaintRepository.countByStatus(IssueStatus.PENDING.name()));
        m.put("inProgressIssues", complaintRepository.countByStatus(IssueStatus.IN_PROGRESS.name()));
        m.put("resolvedIssues", complaintRepository.countByStatus(IssueStatus.RESOLVED.name()));

        Map<String, String> systemHealth = new HashMap<>();
        systemHealth.put("serverStatus", "Online");
        systemHealth.put("database", "Connected");
        systemHealth.put("lastBackup", "N/A");
        m.put("systemHealth", systemHealth);

        List<Map<String, Object>> recentActivity = activityLogRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(log -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("message", log.getMessage());
                    row.put("at", log.getCreatedAt());
                    return row;
                })
                .collect(Collectors.toList());
        m.put("recentActivity", recentActivity);
        return m;
    }
}
