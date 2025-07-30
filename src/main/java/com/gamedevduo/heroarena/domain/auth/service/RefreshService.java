package com.gamedevduo.heroarena.domain.auth.service;


import com.gamedevduo.heroarena.domain.auth.presentation.dto.response.ReissueTokenResponse;

public interface RefreshService {
    ReissueTokenResponse execute(String resolveRefreshToken);
}
