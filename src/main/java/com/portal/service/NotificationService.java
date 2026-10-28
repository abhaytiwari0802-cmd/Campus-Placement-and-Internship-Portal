package com.portal.service;

import com.portal.model.Notification;
import com.portal.model.NotificationType;
import com.portal.model.User;
import com.portal.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification sendNotification(User recipient, User sender, String title, String message, String link, NotificationType type) {
        Notification notification = new Notification(recipient, sender, title, message, link, type);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification sendNotification(User recipient, String title, String message, String link, NotificationType type) {
        return sendNotification(recipient, null, title, message, link, type);
    }


    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(User recipient) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
    }

    @Transactional(readOnly = true)
    public List<Notification> getRecentUserNotifications(User recipient) {
        return notificationRepository.findTop5ByRecipientOrderByCreatedAtDesc(recipient);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User recipient) {
        return notificationRepository.countByRecipientAndIsReadFalse(recipient);
    }

    @Transactional
    public void markAsRead(Long notificationId, User recipient) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getRecipient().getId().equals(recipient.getId())) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            }
        });
    }

    @Transactional
    public void markAllAsRead(User recipient) {
        List<Notification> list = notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
        for (Notification n : list) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(list);
    }
}
