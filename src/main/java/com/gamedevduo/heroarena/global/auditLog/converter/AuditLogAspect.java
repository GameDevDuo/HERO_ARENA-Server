package com.gamedevduo.heroarena.global.auditLog.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamedevduo.heroarena.domain.auditLog.domain.AuditLog;
import com.gamedevduo.heroarena.domain.auditLog.repository.AuditLogRepository;
import com.gamedevduo.heroarena.global.auditLog.annotation.Auditable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditable)")
    public Object logAudit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }

        String userId = getUserIdFromSecurityContext();
        String userAgent = request != null ? request.getHeader("User-Agent") : "no-request";

        String requestBody = extractRequestArgs(joinPoint.getArgs());
        String resourceType = auditable.resourceType();
        String action = auditable.action();

        Object result;
        String responseBody = null;

        try {
            result = joinPoint.proceed();
            responseBody = objectMapper.writeValueAsString(result);
        } catch (Throwable t) {
            responseBody = "{\"error\": \"" + t.getMessage() + "\"}";
            throw t;
        } finally {
            AuditLog log = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .request(requestBody)
                    .response(responseBody)
                    .userAgent(userAgent)
                    .resourceType(resourceType)
                    .build();
            auditLogRepository.save(log);
        }

        return result;
    }

    private String extractRequestArgs(Object[] args) {
        try {
            String json = objectMapper.writeValueAsString(args);
            json = json.replaceAll("(?i)(\"password\"\\s*:\\s*\")[^\"]*(\")", "$1*****$2");
            json = json.replaceAll("(?i)(\"code\"\\s*:\\s*\")[^\"]*(\")", "$1*****$2");
            return json;
        } catch (Exception e) {
            return "Failed to serialize arguments";
        }
    }

    private String getUserIdFromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "anonymous";
    }
}