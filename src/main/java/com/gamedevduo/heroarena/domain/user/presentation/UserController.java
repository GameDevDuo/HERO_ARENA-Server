package com.gamedevduo.heroarena.domain.user.presentation;

import com.gamedevduo.heroarena.domain.user.presentation.dto.response.UserInfoResponse;
import com.gamedevduo.heroarena.domain.user.service.UserService;
import com.gamedevduo.heroarena.global.exception.dto.BaseResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/user")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserInfoResponse>> me() {
        UserInfoResponse userResponse = userService.userInfo();
        return ResponseEntity.ok(BaseResponse.success(userResponse, "내 정보 조회 성공"));
    }

}
