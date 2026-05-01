package com.citizenconnect.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.citizenconnect.entity.ActivityLog;
import com.citizenconnect.repository.ActivityLogRepository;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional
    public void log(String message) {
        ActivityLog log = new ActivityLog();
        log.setMessage(message);
        log.setCreatedAt(LocalDateTime.now());
        activityLogRepository.save(log);
    }
}
