package com.portal.repository;

import com.portal.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByIsActiveTrueOrderByCreatedAtDesc();
    List<Announcement> findAllByOrderByCreatedAtDesc();
    List<Announcement> findByTargetAudienceInAndIsActiveTrueOrderByCreatedAtDesc(List<String> audiences);
}
