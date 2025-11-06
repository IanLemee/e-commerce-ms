package com.tech.ian.user.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class VerificationCodeGenerator {

    public int generate() {
        int code = 0;
        Random random = new Random();
        for (int i = 0; i < 6 ; i++) {
            int r = random.nextInt(1, 10);
            code = (code * 10) + r;
        }
        return code;
    }
}
