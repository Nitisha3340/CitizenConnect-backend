package com.citizenconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.citizenconnect.entity.Announcement;
import com.citizenconnect.entity.Region;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findTop20ByOrderByCreatedAtDesc();

    List<Announcement> findTop20ByRegionOrderByCreatedAtDesc(Region region);
}
