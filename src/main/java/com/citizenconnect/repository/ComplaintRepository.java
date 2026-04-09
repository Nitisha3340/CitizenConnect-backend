package com.citizenconnect.repository;

import java.util.List;
import com.citizenconnect.entity.Region;
import com.citizenconnect.entity.Severity;

import org.springframework.data.jpa.repository.JpaRepository;

import com.citizenconnect.entity.Complaint;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

	List<Complaint> findByUserEmail(String email);

	long countByUserEmail(String email);

	long countByUserEmailAndStatus(String email, String status);

	List<Complaint> findTop5ByUserEmailOrderByCreatedAtDesc(String email);

	long countBySeverity(Severity severity);
	List<Complaint> findByRegion(Region region);
}