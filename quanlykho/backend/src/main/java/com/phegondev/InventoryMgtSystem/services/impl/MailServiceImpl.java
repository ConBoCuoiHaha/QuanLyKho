package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.services.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@example.com}")
    private String fromEmail;

    @Override
    public void guiOTP(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Ma OTP dat lai mat khau");
            message.setText("""
                    Xin chao,

                    Ma OTP dat lai mat khau cua ban la: %s
                    Ma nay co hieu luc trong 5 phut.

                    Neu ban khong yeu cau, vui long bo qua email nay.
                    """.formatted(otp));
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Khong the gui OTP toi {}: {}", to, ex.getMessage());
        }
    }
}
