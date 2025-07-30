package com.gamedevduo.heroarena.domain.auth.service;

import com.gamedevduo.heroarena.domain.auth.presentation.dto.request.AuthCodeRequest;
import com.gamedevduo.heroarena.domain.auth.presentation.dto.request.PasswordChangeRequest;

public interface PasswordChangeService {
    void sendMail(AuthCodeRequest request);

    void passwordChange(PasswordChangeRequest request);
}
