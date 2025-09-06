package com.orphanagehub.util;

import io.vavr.Function1;
import io.vavr.control.Try;
import java.util.regex.Pattern;

public class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    
    public static final Function1<String, Try<Boolean>> isValidEmail = email ->
        Try.of(() -> EMAIL_PATTERN.matcher(email).matches());
    
    public static final Function1<String, Try<Boolean>> isValidUsername = username ->
        Try.of(() -> USERNAME_PATTERN.matcher(username).matches());
    
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
}

