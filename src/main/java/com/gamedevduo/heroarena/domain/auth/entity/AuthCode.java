package com.gamedevduo.heroarena.domain.auth.entity;

import com.gamedevduo.heroarena.domain.auth.entity.enums.VerifyCodeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthCode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String email;

    @Column(length = 5)
    private String code;

    private LocalDateTime authCodeExpiresAt;

    @Enumerated(EnumType.STRING)
    private VerifyCodeType type;

    private boolean emailVerifyStatus;

    public boolean isAuthCodeExpired() {
        return LocalDateTime.now().isAfter(this.authCodeExpiresAt);
    }

    public void updateEmailVerifyStatus(boolean emailVerifyStatus) {
        this.emailVerifyStatus = emailVerifyStatus;
    }
}
