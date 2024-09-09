package com.kmbbj.backend.global.notifications.every_email_service;


import com.kmbbj.backend.feature.auth.entity.User;
import com.kmbbj.backend.global.notifications.entity.EmailAlarm;
import com.kmbbj.backend.global.notifications.repository.EmailAlarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class EveryEmailService {
    @Value("${MAIL_USERNAME}")
    private String mailUsername;

    // JavaMailSender를 주입받아 이메일 발송 가능
    private final EmailAlarmRepository emailAlarmRepository;
    private final JavaMailSender mailSender;

    /**
     * 이메일 메시지를 보내는 메소드
     * @param user 수신자 user 객체
     * @param to 수신자 이메일 주소
     * @param subject 이메일 제목
     * @param text 이메일 본문
     * @param type String enum 값 (BYE, SELL, START, END, 공백 추가)
     */

    public void sendSimpleMessage(User user, String to, String subject, String text, String type) {

        // email_alarms 테이블 저장
        EmailAlarm emailAlarm = new EmailAlarm();
        emailAlarm.setUser(user);
        emailAlarm.setSubject(subject);
        emailAlarm.setMessage(text);
        emailAlarm.setTradeOrder(EmailAlarm.TradeOrder.valueOf(type));
        emailAlarm.setCreateDateAlarms(new Timestamp(System.currentTimeMillis()));
        emailAlarmRepository.save(emailAlarm);

        // SimpleMailMessage 객체 생성 및 설정
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailUsername);
        message.setTo(to); // 수신자 설정
        message.setSubject(subject); // 제목 설정
        message.setText(text); // 본문 설정
        // 이메일 전송
        mailSender.send(message);
    }
}