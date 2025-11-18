package com.tech.ian.notification.service;

import com.tech.ian.notification.config.dto.EmailEventDto;
import com.tech.ian.notification.utils.EmailTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailSenderService {

    private final EmailTemplate emailTemplate;

    public EmailSenderService(EmailTemplate emailTemplate) {
        this.emailTemplate = emailTemplate;
    }

    @KafkaListener(topics = "email-verification-topic", groupId = "notification-service-group")
    public void eventListener(EmailEventDto dto) {
        emailTemplate.sendEmailVerificationCode(dto);
    }
}
