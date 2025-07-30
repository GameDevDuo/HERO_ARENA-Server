package com.gamedevduo.heroarena.domain.auditLog.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String userId;

    @Column
    private String request;

    @Column
    private String response;

    /**
     * 수행된 동작에 대한 값이다.
     * ex. UPDATE, DELETE, CREATE
     */
    @Column
    private String action;

    /**
     * 클라이언트에 따라 다른 값이 들어갈 수 있다.
     * ex. 브라우저, 디바이스
     */
    @Column(columnDefinition = "text")
    private String userAgent;

    /**
     * 변경된 자원 타입의 값이다.
     */
    @Column
    private String resourceType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
