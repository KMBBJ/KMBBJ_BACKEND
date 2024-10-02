package com.kmbbj.backend.notifications.loginemail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class LoginEmailService {

    // JavaMailSender를 주입받아 이메일 발송 가능
    private final JavaMailSender mailSender;

    /**
     * 이메일 메시지를 보내는 메소드
     * @param to 수신자 이메일 주소
     * @param subject 이메일 제목
     * @param text 이메일 본문
     */

    public void sendSimpleMessage(String to, String subject, String text) {

        // SimpleMailMessage 객체 생성 및 설정
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to); // 수신자 설정
        message.setSubject(subject); // 제목 설정
        message.setText(text); // 본문 설정
        // 이메일 전송
        mailSender.send(message);
    }

    public void sendHtmlMessage2(String to, String subject, String htmlContent) throws MessagingException {
        // MimeMessage 객체 생성
        MimeMessage message = mailSender.createMimeMessage();

        // MimeMessageHelper 객체를 사용해 이메일 설정
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to); // 수신자 설정
        helper.setSubject(subject); // 제목 설정
        helper.setText(htmlContent, true); // HTML 형식 본문 설정, true로 설정하면 HTML 포맷

        // 이메일 전송
        mailSender.send(message);
    }
}