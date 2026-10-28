package com.portal.repository;

import com.portal.model.Notification;
import com.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
    List<Notification> findTop5ByRecipientOrderByCreatedAtDesc(User recipient);
    long countByRecipientAndIsReadFalse(User recipient);
}
