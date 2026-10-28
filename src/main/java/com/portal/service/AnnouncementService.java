package com.portal.service;

import com.portal.dto.AnnouncementDto;
import com.portal.exception.ResourceNotFoundException;
import com.portal.model.Announcement;
import com.portal.model.User;
import com.portal.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    @Transactional(readOnly = true)
    public List<Announcement> getActiveAnnouncements() {
        return announcementRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Announcement> getAnnouncementsForAudience(String audience) {
        return announcementRepository.findByTargetAudienceInAndIsActiveTrueOrderByCreatedAtDesc(
                Arrays.asList("ALL", audience)
        );
    }

    @Transactional(readOnly = true)
    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Announcement getById(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));
    }

    @Transactional
    public Announcement createAnnouncement(AnnouncementDto dto, User admin) {
        Announcement announcement = new Announcement(
                admin,
                dto.getTitle(),
                dto.getContent(),
                dto.getTargetAudience() != null ? dto.getTargetAudience() : "ALL",
                dto.getPriority() != null ? dto.getPriority() : "NORMAL"
        );
        return announcementRepository.save(announcement);
    }

    @Transactional
    public void toggleActiveStatus(Long id) {
        Announcement announcement = getById(id);
        announcement.setIsActive(!announcement.getIsActive());
        announcementRepository.save(announcement);
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        announcementRepository.deleteById(id);
    }
}
