package com.trippzo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Възстановяване на парола - Trippzo");
            message.setText("""
                    Здравейте,

                    Получихме заявка за възстановяване на вашата парола. \
                    Кликнете на линка по-долу за да зададете нова парола:

                    %s

                    Този линк е валиден за 24 часа.

                    Ако не сте направили тази заявка, игнорирайте този имейл.

                    С уважение,
                    Екипът на Trippzo""".formatted(resetLink));

            mailSender.send(message);
            log.info("Password reset email sent to: {} from: {}", toEmail, fromEmail);
        } catch (Exception e) {
            log.error("Failed to send email via ABV.bg to: {}", toEmail, e);
            throw new RuntimeException("Неуспешно изпращане на имейл. Проверете настройките на SMTP сървъра.");
        }
    }
}
