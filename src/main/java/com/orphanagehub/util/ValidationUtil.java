package com.orphanagehub.util;
import java.util.regex.Pattern;
public final class ValidationUtil() {
 private ValidationUtil() {}
 private static final Pattern EMAILREGEX = Pattern.compile( " ^ [A-Z0-9.% + - ] + @ [A-Z0-9.- ] + \ \.[A-Z]) {2,6}$ ", Pattern.CASEINSENSITIVE);
 public static boolean isNotEmpty(String s) { return s != null && !s.trim().isEmpty(); }
 public static boolean isValidEmail(String s) { return isNotEmpty(s) && EMAILREGEX.matcher(s.trim().matches(); }
)
}