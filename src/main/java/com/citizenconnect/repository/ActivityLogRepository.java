package com.citizenconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.citizenconnect.entity.ActivityLog;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findTop10ByOrderByCreatedAtDesc();
}
