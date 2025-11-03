package com.tech.ian.user.utils;

import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.stream.IntStream;

@Component
public class VerificationCodeGenerator {
    private int code = 0;

    public int generate() {
        Random random = new Random();
        IntStream ints = random.ints(1, 10);
        ints.limit(6).forEach((i) -> {
            code = (code * 10) + i;
        });

        return code;
    }
}
