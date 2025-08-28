/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.util;

import java.util.regex.Pattern;

public final class ValidationUtil {
    private ValidationUtil() {}

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public static boolean isNonEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return isNonEmpty(email) && EMAIL.matcher(email).matches();
    }

    public static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
