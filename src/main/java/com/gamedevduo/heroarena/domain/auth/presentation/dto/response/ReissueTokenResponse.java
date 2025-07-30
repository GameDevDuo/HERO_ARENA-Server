package com.gamedevduo.heroarena.domain.auth.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReissueTokenResponse {
    private final String accessToken;
    private final LocalDateTime accessTokenExpiredAt;
    private final String refreshToken;
    private final LocalDateTime refreshTokenExpiredAt;
}
