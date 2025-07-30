package com.gamedevduo.heroarena.domain.auth.service.impl;

import com.gamedevduo.heroarena.domain.auth.presentation.dto.request.FindNameRequest;
import com.gamedevduo.heroarena.domain.auth.presentation.dto.response.FindNameResponse;
import com.gamedevduo.heroarena.domain.auth.service.FindNameService;
import com.gamedevduo.heroarena.domain.user.entity.User;
import com.gamedevduo.heroarena.domain.user.repository.UserRepository;
import com.gamedevduo.heroarena.global.exception.HttpException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindNameServiceImpl implements FindNameService {
    private final UserRepository userRepository;

    public FindNameResponse findNameByEmail(FindNameRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "해당 이메일의 유저가 없습니다."));
        return new FindNameResponse(user.getName());
    }
}
