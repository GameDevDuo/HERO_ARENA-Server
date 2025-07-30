package com.gamedevduo.heroarena.domain.auth.service;


import com.gamedevduo.heroarena.domain.auth.presentation.dto.request.AuthCodeRequest;
import com.gamedevduo.heroarena.domain.auth.presentation.dto.request.EmailVerifyRequest;
import com.gamedevduo.heroarena.domain.auth.presentation.dto.request.SignupRequest;

public interface SignupService {
    void signup(SignupRequest request);
    void sendSignupMail(AuthCodeRequest request);
    void emailVerify(EmailVerifyRequest request);
}
