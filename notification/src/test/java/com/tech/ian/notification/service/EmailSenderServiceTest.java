package com.tech.ian.notification.service;

import com.tech.ian.notification.utils.EmailTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailSenderServiceTest {

    @Mock
    private EmailTemplate emailTemplate;

    @InjectMocks
    private EmailSenderService emailSenderService;

    @Test
    void shouldCallMethodSendAtLeastOnce() {
        doNothing().when(emailTemplate).sendEmailVerificationCode(null);
        emailSenderService.eventListener(null);
        verify(emailTemplate, times(1)).sendEmailVerificationCode(null);
    }
}