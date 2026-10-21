package com.printpass.user.utils;

import java.security.SecureRandom;

public class OtpGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();

    // Private constructor taake koi is class ka object na bana sake (Utility class)
    private OtpGenerator() {}

    public static String generateSixDigitOtp() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }
}