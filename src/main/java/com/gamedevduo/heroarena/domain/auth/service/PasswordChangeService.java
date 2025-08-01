package com.gamedevduo.heroarena.domain.auth.service;

import com.gamedevduo.heroarena.domain.auth.presentation.dto.request.AuthCodeRequest;
import com.gamedevduo.heroarena.domain.auth.presentation.dto.request.EmailVerifyRequest;
import com.gamedevduo.heroarena.domain.auth.presentation.dto.request.PasswordChangeRequest;

public interface PasswordChangeService {
    void sendMail(AuthCodeRequest request);
    void emailVerify(EmailVerifyRequest request);
    void passwordChange(PasswordChangeRequest request);
}
