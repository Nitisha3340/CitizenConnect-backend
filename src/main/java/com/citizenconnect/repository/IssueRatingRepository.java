package com.citizenconnect.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.citizenconnect.entity.IssueRating;
import com.citizenconnect.entity.Region;

public interface IssueRatingRepository extends JpaRepository<IssueRating, Long> {

    List<IssueRating> findByComplaint_Id(Long complaintId);

    boolean existsByComplaint_IdAndCitizen_Id(Long complaintId, Long citizenId);

    @Query("SELECT r FROM IssueRating r JOIN r.complaint c WHERE c.region = :region AND r.createdAt >= :since")
    List<IssueRating> findByComplaintRegionSince(@Param("region") Region region, @Param("since") LocalDateTime since);
}
