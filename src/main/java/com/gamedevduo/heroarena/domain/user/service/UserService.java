package com.gamedevduo.heroarena.domain.user.service;



import com.gamedevduo.heroarena.domain.user.entity.User;
import com.gamedevduo.heroarena.domain.user.presentation.dto.response.UserInfoResponse;
import com.gamedevduo.heroarena.domain.user.repository.UserRepository;
import com.gamedevduo.heroarena.global.exception.HttpException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    @Transactional
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."));
    }

    @Transactional
    public UserInfoResponse userInfo() {
        User user = getCurrentUser();

        return UserInfoResponse.builder()
                .name(user.getName())
                .build();
    }
}
