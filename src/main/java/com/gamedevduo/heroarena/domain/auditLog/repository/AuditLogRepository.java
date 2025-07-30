package com.gamedevduo.heroarena.domain.auditLog.repository;


import com.gamedevduo.heroarena.domain.auditLog.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
