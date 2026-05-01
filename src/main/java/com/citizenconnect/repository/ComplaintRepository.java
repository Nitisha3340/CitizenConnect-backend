package com.citizenconnect.repository;

import java.util.List;
import com.citizenconnect.entity.Region;
import com.citizenconnect.entity.Severity;

import org.springframework.data.jpa.repository.JpaRepository;

import com.citizenconnect.entity.Complaint;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

	List<Complaint> findByUser_Email(String email);

	long countByUser_Email(String email);

	long countByUser_EmailAndStatus(String email, String status);

	List<Complaint> findTop5ByUser_EmailOrderByCreatedAtDesc(String email);

	long countBySeverity(Severity severity);

	List<Complaint> findByRegion(Region region);

	List<Complaint> findByRegionOrderByCreatedAtDesc(Region region);

	long countByRegion(Region region);

	long countByRegionAndStatus(Region region, String status);

	long countByStatus(String status);

	long countByRegionAndStatusAndResolvedAtBetween(
			Region region,
			String status,
			java.time.LocalDateTime start,
			java.time.LocalDateTime end);

	List<Complaint> findByRegionAndSeverityOrderByCreatedAtDesc(Region region, Severity severity);

	long countByRegionAndSeverity(Region region, Severity severity);

	List<Complaint> findTop3ByRegionAndSeverityOrderByCreatedAtDesc(Region region, Severity severity);

	List<Complaint> findTop3BySeverityOrderByCreatedAtDesc(Severity severity);
}