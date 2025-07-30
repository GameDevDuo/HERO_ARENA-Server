package com.gamedevduo.heroarena.domain.auth.service.impl;

import com.gamedevduo.heroarena.domain.auth.presentation.dto.request.SigninRequest;
import com.gamedevduo.heroarena.domain.auth.presentation.dto.response.SignInResponse;
import com.gamedevduo.heroarena.domain.auth.service.SigninService;
import com.gamedevduo.heroarena.domain.user.entity.User;
import com.gamedevduo.heroarena.domain.user.repository.UserRepository;
import com.gamedevduo.heroarena.global.exception.HttpException;
import com.gamedevduo.heroarena.global.security.jwt.JwtProvider;
import com.gamedevduo.heroarena.global.security.jwt.dto.JwtDetails;
import com.gamedevduo.heroarena.global.security.jwt.enums.JwtType;
import com.gamedevduo.heroarena.global.util.RedisUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SigninServiceImpl implements SigninService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisUtil redisUtil;

    @Value("${jwt.refreshTokenExpires}")
    private long refreshTokenTtl;
    @Transactional
    public SignInResponse execute(SigninRequest request) {
        User user = userRepository.findByName(request.getName())
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "없는 유저 입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new HttpException(HttpStatus.UNAUTHORIZED, "비밀번호가 틀렸습니다.");
        }

        JwtDetails accessToken = jwtProvider.generateToken(user.getId(), JwtType.ACCESS_TOKEN);
        JwtDetails refreshToken = jwtProvider.generateToken(user.getId(), JwtType.REFRESH_TOKEN);

        redisUtil.setRefreshToken(user.getId().toString(), refreshToken.token(), refreshTokenTtl);

        return SignInResponse.builder()
                .accessToken(accessToken.token())
                .refreshToken(refreshToken.token())
                .accessTokenExpiredAt(accessToken.expiredAt())
                .refreshTokenExpiredAt(refreshToken.expiredAt())
                .build();
    }
}
