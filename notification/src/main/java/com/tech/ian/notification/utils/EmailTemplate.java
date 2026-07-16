package com.tech.ian.notification.utils;

import com.tech.ian.notification.config.dto.EmailEventDto;
import com.tech.ian.notification.config.dto.OrderEventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplate {

    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String email;

    public EmailTemplate(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmailVerificationCode(EmailEventDto dto) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(email);
        simpleMailMessage.setTo(dto.email());
        simpleMailMessage.setSubject("Activation code");
        simpleMailMessage.setText("Your activation code is " + dto.code());
        mailSender.send(simpleMailMessage);
    }

    public void sendEmailOrderConfirmation(OrderEventDto dto) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(email);
        simpleMailMessage.setTo(dto.customerId());
        simpleMailMessage.setSubject("Payment Approved");
        simpleMailMessage.setText("Payment Approved");
    }
}
