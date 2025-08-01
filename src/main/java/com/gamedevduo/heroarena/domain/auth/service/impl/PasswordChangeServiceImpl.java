package com.gamedevduo.heroarena.domain.auth.service.impl;

import com.gamedevduo.heroarena.domain.auth.entity.AuthCode;
import com.gamedevduo.heroarena.domain.auth.entity.enums.VerifyCodeType;
import com.gamedevduo.heroarena.domain.auth.presentation.dto.request.AuthCodeRequest;
import com.gamedevduo.heroarena.domain.auth.presentation.dto.request.EmailVerifyRequest;
import com.gamedevduo.heroarena.domain.auth.presentation.dto.request.PasswordChangeRequest;
import com.gamedevduo.heroarena.domain.auth.repository.AuthCodeRepository;
import com.gamedevduo.heroarena.domain.auth.service.PasswordChangeService;
import com.gamedevduo.heroarena.domain.user.entity.User;
import com.gamedevduo.heroarena.domain.user.repository.UserRepository;
import com.gamedevduo.heroarena.global.auditLog.annotation.Auditable;
import com.gamedevduo.heroarena.global.exception.HttpException;
import com.gamedevduo.heroarena.global.util.RandomUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordChangeServiceImpl implements PasswordChangeService {
    private final AuthCodeRepository authCodeRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RandomUtil randomUtil;

    @Transactional
    public void sendMail(AuthCodeRequest request) {
        authCodeRepository.deleteByEmail(request.getEmail());

        AuthCode passwordChangeCode = AuthCode.builder()
            .email(request.getEmail())
            .type(VerifyCodeType.PASSWORD_RESET)
            .code(randomUtil.generateCode())
            .authCodeExpiresAt(LocalDateTime.now().plusMinutes(3))
            .build();

        authCodeRepository.save(passwordChangeCode);

        String htmlContent = String.format("""
    <!DOCTYPE html>
    <html lang="ko">
    <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>HERO ARENA 비밀번호 재설정</title>
    </head>
    <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #1a1a1a;">
        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 0; padding: 20px;">
            <tr>
                <td align="center">
                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" style="max-width: 600px; width: 100%%; background-color: #2b2b2b; border-radius: 12px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5); overflow: hidden;">
                        <tr>
                            <td style="background-color: #5A3E85; padding: 40px 30px; text-align: center;">
                                <h1 style="margin: 0 0 8px 0; font-size: 36px; font-weight: 700; color: white; letter-spacing: -0.5px;">HERO ARENA</h1>
                                <p style="margin: 0; color: rgba(255, 255, 255, 0.85); font-size: 16px; font-weight: 400;">실시간 멀티 경쟁 게임</p>
                            </td>
                        </tr>
                        <tr>
                            <td style="padding: 40px 30px;">
                                <h2 style="margin: 0 0 16px 0; font-size: 24px; color: #F2F2F2; text-align: center; font-weight: 600;">비밀번호 재설정 요청</h2>
                                
                                <p style="margin: 0 0 32px 0; color: #CCCCCC; line-height: 1.6; text-align: center; font-size: 15px;">
                                    HERO ARENA 계정의 비밀번호를 재설정하려면 아래 인증코드를 입력해주세요.<br>
                                    요청하지 않으셨다면 이 메일을 무시하셔도 됩니다.
                                </p>

                                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 32px 0;">
                                    <tr>
                                        <td style="background-color: #2D223C; border: 2px solid #F2C94C; border-radius: 12px; padding: 32px; text-align: center;">
                                            <p style="margin: 0 0 16px 0; font-size: 14px; color: #F2C94C; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">인증 코드</p>
                                            <p style="margin: 0; font-size: 32px; font-weight: 700; color: #F2C94C; letter-spacing: 6px; font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', monospace;">%s</p>
                                        </td>
                                    </tr>
                                </table>
                                
                                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 0;">
                                    <tr>
                                        <td style="background-color: #3B2F2F; border: 1px solid #F2994A; border-radius: 8px; padding: 16px; text-align: center;">
                                            <p style="margin: 0; color: #F2994A; font-size: 14px;">⚠️ 이 인증코드는 3분간 유효합니다. 본인이 요청하지 않았다면 이 메일을 무시해주세요.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        
                        <tr>
                            <td style="background-color: #202020; padding: 24px 30px; text-align: center; border-top: 1px solid #333;">
                                <p style="margin: 0 0 8px 0; color: #AAAAAA; font-size: 13px; line-height: 1.5;">
                                    이 메일은 비밀번호 재설정을 요청하신 분에게만 발송됩니다.<br>
                                    문의사항이 있으시면 <a href="mailto:seungkkyu0@gmail.com" style="color: #F2C94C; text-decoration: none;">이 메일로 문의해주세요</a>.
                                </p>
                                <p style="margin: 0; color: #F2C94C; font-weight: 600; font-size: 14px;">HERO ARENA 팀</p>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </body>
    </html>
    """, passwordChangeCode.getCode());


        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(passwordChangeCode.getEmail());
            helper.setSubject("HERO ARENA | 비밀번호 재설정 코드입니다.");
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }

    @Transactional
    public void emailVerify(EmailVerifyRequest request) {
        AuthCode code = authCodeRepository.findByEmail(request.getEmail());

        if (code == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "인증 코드를 찾을 수 없습니다.");
        }

        if (code.isAuthCodeExpired()) {
            authCodeRepository.deleteByEmail(request.getEmail());
            throw new HttpException(HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다.");
        }

        if (!code.getCode().equals(request.getCode())) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "잘못된 인증 코드입니다.");
        }

        code.updateEmailVerifyStatus(true);
    }


    @Transactional
    @Auditable(action = "UPDATE", resourceType = "User")
    public void passwordChange(PasswordChangeRequest request) {
        AuthCode code = authCodeRepository.findByEmail(request.getEmail());
        if (!code.isEmailVerifyStatus()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "인증되지 않은 비밀번호 요청 입니다.");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "해당 이메일의 사용자가 존재하지 않습니다."));
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.updatePassword(encodedPassword);
        authCodeRepository.deleteByEmail(request.getEmail());
    }
}
