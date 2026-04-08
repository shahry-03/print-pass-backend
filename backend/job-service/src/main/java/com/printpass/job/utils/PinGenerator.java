package com.printpass.job.utils;

import java.security.SecureRandom;

public class PinGenerator {
    private static final SecureRandom random = new SecureRandom();

    public static String generateSixDigitPin() {
        return String.format("%06d", random.nextInt(1000000));
    }
}