package com.gamedevduo.heroarena.domain.auth.service;


import com.gamedevduo.heroarena.domain.auth.presentation.dto.request.SigninRequest;
import com.gamedevduo.heroarena.domain.auth.presentation.dto.response.SignInResponse;

public interface SigninService {
    SignInResponse execute(SigninRequest request);
}
