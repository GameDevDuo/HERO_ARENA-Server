package com.gamedevduo.heroarena.domain.auth.presentation;

import com.gamedevduo.heroarena.domain.auth.presentation.dto.request.*;
import com.gamedevduo.heroarena.domain.auth.presentation.dto.response.FindNameResponse;
import com.gamedevduo.heroarena.domain.auth.presentation.dto.response.ReissueTokenResponse;
import com.gamedevduo.heroarena.domain.auth.presentation.dto.response.SignInResponse;
import com.gamedevduo.heroarena.domain.auth.service.*;
import com.gamedevduo.heroarena.global.exception.dto.BaseResponse;
import com.gamedevduo.heroarena.global.security.jwt.JwtProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final SignupService signupService;
    private final SigninService signinService;
    private final RefreshService refreshService;
    private final PasswordChangeService passwordChangeService;
    private final LogoutService logoutService;
    private final FindNameService findNameService;
    private final JwtProvider jwtProvider;

    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<Void>> signup(@RequestBody @Valid SignupRequest request) {
        signupService.signup(request);
        return ResponseEntity.ok(BaseResponse.success("회원가입 성공"));
    }

    @PostMapping("/signup/mailsend")
    public ResponseEntity<BaseResponse<Void>> signupMailSend(@RequestBody @Valid AuthCodeRequest request) {
        signupService.sendSignupMail(request);
        return ResponseEntity.ok(BaseResponse.success("회원가입 이메일 발송 성공"));
    }

    @PostMapping("/signup/emailverify")
    public ResponseEntity<BaseResponse<Void>> signupEmailVerify(@RequestBody @Valid EmailVerifyRequest request) {
        signupService.emailVerify(request);
        return ResponseEntity.ok(BaseResponse.success("이메일 인증 성공"));
    }

    @PostMapping("/signin")
    public ResponseEntity<BaseResponse<SignInResponse>> signin(@RequestBody @Valid SigninRequest request) {
        SignInResponse response = signinService.execute(request);
        return ResponseEntity.ok(BaseResponse.success(response, "로그인 성공"));
    }

    @PostMapping("/reissue")
    public ResponseEntity<BaseResponse<ReissueTokenResponse>> reissueToken(@RequestHeader("Refresh-Token") String refreshHeader) {
        String refreshToken = jwtProvider.resolveToken(refreshHeader);
        ReissueTokenResponse response = refreshService.execute(refreshToken);
        return ResponseEntity.ok(BaseResponse.success(response, "토큰 재발급 성공"));
    }

    @PostMapping("/pwchange/mailsend")
    public ResponseEntity<BaseResponse<Void>> mailSend(@RequestBody @Valid AuthCodeRequest request) {
        passwordChangeService.sendMail(request);
        return ResponseEntity.ok(BaseResponse.success("비밀번호 변경 메일 발송 성공"));
    }
    @PostMapping("/pwchange/emailverify")
    public ResponseEntity<BaseResponse<Void>> pwchangeEmailVerify(@RequestBody @Valid EmailVerifyRequest request) {
        passwordChangeService.emailVerify(request);
        return ResponseEntity.ok(BaseResponse.success("비밀번호 변경 인증 성공"));
    }
    @PostMapping("/pwchange")
    public ResponseEntity<BaseResponse<Void>> mailCheck(@RequestBody @Valid PasswordChangeRequest request) {
        passwordChangeService.passwordChange(request);
        return ResponseEntity.ok(BaseResponse.success("비밀번호 변경 성공"));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(@RequestHeader("Refresh-Token") String refreshToken) {
        String resolveRefreshToken = jwtProvider.resolveToken(refreshToken);
        logoutService.logout(resolveRefreshToken);
        return ResponseEntity.ok(BaseResponse.success("로그아웃 성공"));
    }

    @PostMapping("/find-name")
    public ResponseEntity<BaseResponse<FindNameResponse>> findName(@RequestBody @Valid FindNameRequest request) {
        FindNameResponse name = findNameService.findNameByEmail(request);
        return ResponseEntity.ok(BaseResponse.success(name, "이름 조회 성공"));
    }
}
