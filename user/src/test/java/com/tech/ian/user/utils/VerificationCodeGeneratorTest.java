package com.tech.ian.user.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VerificationCodeGeneratorTest {

    @InjectMocks
    private VerificationCodeGenerator verificationCodeGenerator;

    @Test
    void shouldReturnSixDigits() {
        int generate = verificationCodeGenerator.generate();

        assertTrue(generate >= 100_000);
        assertTrue(generate <= 999_999);
    }
}