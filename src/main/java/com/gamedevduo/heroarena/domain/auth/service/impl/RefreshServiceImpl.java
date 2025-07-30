package com.gamedevduo.heroarena.domain.auth.service.impl;

import com.gamedevduo.heroarena.domain.auth.presentation.dto.response.ReissueTokenResponse;
import com.gamedevduo.heroarena.domain.auth.service.RefreshService;
import com.gamedevduo.heroarena.global.exception.HttpException;
import com.gamedevduo.heroarena.global.security.jwt.JwtProvider;
import com.gamedevduo.heroarena.global.security.jwt.dto.JwtDetails;
import com.gamedevduo.heroarena.global.security.jwt.enums.JwtType;
import com.gamedevduo.heroarena.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshServiceImpl implements RefreshService {
    private final JwtProvider jwtProvider;
    private final RedisUtil redisUtil;
    @Value("${jwt.refreshTokenExpires}")
    private long refreshTokenTtl;
    public ReissueTokenResponse execute(String resolveRefreshToken) {
        if (!jwtProvider.validateToken(resolveRefreshToken, JwtType.REFRESH_TOKEN)) {
            throw new HttpException(HttpStatus.FORBIDDEN, "만료되었거나 잘못된 리프레시 토큰입니다.");
        }

        String userId = jwtProvider.getIdByRefreshToken(resolveRefreshToken);

        String storedRefreshToken = redisUtil.getRefreshToken(userId)
                .orElseThrow(() -> new HttpException(HttpStatus.FORBIDDEN, "저장된 리프레시 토큰이 없습니다."));

        if (!resolveRefreshToken.equals(storedRefreshToken)) {
            throw new HttpException(HttpStatus.FORBIDDEN, "리프레시 토큰이 일치하지 않습니다.");
        }

        JwtDetails newAccessToken = jwtProvider.generateToken(Long.parseLong(userId), JwtType.ACCESS_TOKEN);
        JwtDetails newRefreshToken = jwtProvider.generateToken(Long.parseLong(userId), JwtType.REFRESH_TOKEN);

        redisUtil.setRefreshToken(userId, newRefreshToken.token(), refreshTokenTtl);

        return new ReissueTokenResponse(
                newAccessToken.token(),
                newAccessToken.expiredAt(),
                newRefreshToken.token(),
                newRefreshToken.expiredAt()
        );
    }
}
