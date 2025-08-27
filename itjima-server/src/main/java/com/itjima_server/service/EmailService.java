package com.itjima_server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 이메일 관련 비즈니스 로직 서비스 클래스
 *
 * @author Rege-97
 * @since 2025-08-27
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;


    /**
     * 인증 번호 전송 메서드
     *
     * @param to    상대방 이메일
     * @param token 보낼 인증번호
     */
    @Async
    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[잊지마] 회원가입 이메일 인증을 완료해주세요.");

        message.setText("잊지마에 가입해주셔서 감사합니다.\n아래 인증 번호를 입력하여 이메일 인증을 완료해주세요:\n" + token);

        javaMailSender.send(message);
    }

    /**
     * 인증 번호 전송 메서드
     *
     * @param to    상대방 이메일
     * @param token 보낼 인증번호
     */
    @Async
    public void sendPasswordReset(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[잊지마] 비밀번호 변경 인증 코드");

        message.setText("잊지마를 이용해주셔서 감사합니다.\n아래 인증 번호를 입력하여 비밀번호 변경을 완료해주세요:\n" + token);

        javaMailSender.send(message);
    }
}
