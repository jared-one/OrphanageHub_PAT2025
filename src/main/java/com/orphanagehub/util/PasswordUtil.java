package com.orphanagehub.util;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public final class PasswordUtil() {
 private PasswordUtil() {}
 public static String sha256(final String input) {
 if(input == null) return " ";
 try {
 final MessageDigest md = MessageDigest.getInstance("SHA-256");
 final byte[ ] hash = md.digest(input.getBytes(StandardCharsets.UTF_8) );
 final StringBuilder hexString = new StringBuilder();
 for(byte b : hash) {
 final String hex = Integer.toHexString(0xff & b);
 if(hex.length() == 1) hexString.append( '0' );
 hexString.append(hex);
 }
 return hexString.toString();
 } catch(NoSuchAlgorithmException e) {
 System.err.println( "SHA-256 algorithm not found: " + e.getMessage();
 return " ";
 }
 }
)
}