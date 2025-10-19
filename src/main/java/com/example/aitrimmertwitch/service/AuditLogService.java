package com.example.aitrimmertwitch.service;

import com.example.aitrimmertwitch.entity.AuditLog;
import com.example.aitrimmertwitch.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(String actor, String action, String details) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setAction(action);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}
