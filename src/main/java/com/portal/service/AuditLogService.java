package com.portal.service;

import com.portal.model.AuditLog;
import com.portal.model.User;
import com.portal.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(User user, String action, String entityType, Long entityId, String details, String ipAddress) {
        AuditLog log = new AuditLog(user, action, entityType, entityId, details, ipAddress);
        auditLogRepository.save(log);
    }

    @Transactional
    public void log(User user, String action, String entityType, Long entityId, String details) {
        log(user, action, entityType, entityId, details, "127.0.0.1");
    }


    @Transactional(readOnly = true)
    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByCreatedAtDesc();
    }
}
