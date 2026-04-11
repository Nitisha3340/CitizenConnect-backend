package com.citizenconnect.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.citizenconnect.dto.AnnouncementRequestDTO;
import com.citizenconnect.dto.AnnouncementResponseDTO;
import com.citizenconnect.entity.Announcement;
import com.citizenconnect.entity.Region;
import com.citizenconnect.entity.Role;
import com.citizenconnect.entity.User;
import com.citizenconnect.repository.AnnouncementRepository;
import com.citizenconnect.repository.UserRepository;
import com.citizenconnect.security.JwtUtil;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository, UserRepository userRepository) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AnnouncementResponseDTO publish(String token, AnnouncementRequestDTO dto) {
        String email = JwtUtil.extractEmail(token);
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (author.getRole() != Role.POLITICIAN) {
            throw new RuntimeException("Only politicians can publish announcements");
        }

        Announcement a = new Announcement();
        a.setContent(dto.getContent());
        a.setRegion(dto.getRegion() != null ? dto.getRegion() : author.getRegion());
        a.setAuthor(author);
        a.setCreatedAt(LocalDateTime.now());
        Announcement saved = announcementRepository.save(a);
        return map(saved);
    }

    public List<AnnouncementResponseDTO> listForCitizen(String token) {
        String email = JwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Region region = user.getRegion();
        List<Announcement> list = region == null
                ? announcementRepository.findTop20ByOrderByCreatedAtDesc()
                : announcementRepository.findTop20ByRegionOrderByCreatedAtDesc(region);
        return list.stream().map(this::map).collect(Collectors.toList());
    }

    public List<AnnouncementResponseDTO> listForPolitician(String token) {
        String email = JwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Region region = user.getRegion();
        List<Announcement> list = region == null
                ? announcementRepository.findTop20ByOrderByCreatedAtDesc()
                : announcementRepository.findTop20ByRegionOrderByCreatedAtDesc(region);
        return list.stream().map(this::map).collect(Collectors.toList());
    }

    private AnnouncementResponseDTO map(Announcement a) {
        AnnouncementResponseDTO dto = new AnnouncementResponseDTO();
        dto.setId(a.getId());
        dto.setContent(a.getContent());
        dto.setAuthorName(a.getAuthor() != null ? a.getAuthor().getName() : null);
        dto.setRegion(a.getRegion());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}
