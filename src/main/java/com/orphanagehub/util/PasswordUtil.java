/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class PasswordUtil {

    private PasswordUtil() {}

    public static String sha256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }

    public static boolean verify(String inputPassword, String storedHash) {
        String inputHash = sha256(inputPassword);
        return inputHash.equals(storedHash);
    }
}
