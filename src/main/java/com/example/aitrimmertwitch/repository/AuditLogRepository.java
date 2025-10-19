package com.example.aitrimmertwitch.repository;

import com.example.aitrimmertwitch.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
