package com.orphanagehub.util;

import io.vavr.Function1;
import io.vavr.control.Try;
import io.vavr.collection.List;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Comprehensive validation utility for all OrphanageHub input fields.
 * Uses functional programming with Vavr for safe validation.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public class ValidationUtil {

    // Email pattern - more robust than original
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Username: 3-20 chars, alphanumeric and underscore
    private static final Pattern USERNAME_PATTERN =
        Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    // South African phone number format: 0xxxxxxxxx (10 digits)
    private static final Pattern SA_PHONE_PATTERN =
        Pattern.compile("^0[1-8][0-9]{8}$");

    // South African ID number format (13 digits)
    private static final Pattern SA_ID_PATTERN =
        Pattern.compile("^[0-9]{13}$");

    // Registration/Tax number format for orphanages
    private static final Pattern REGISTRATION_NUMBER_PATTERN =
        Pattern.compile("^NPO-\\d{3}-\\d{4}$");

    // Bank account number (typically 10-11 digits in SA)
    private static final Pattern BANK_ACCOUNT_PATTERN =
        Pattern.compile("^[0-9]{10,11}$");

    // Postal code (4 digits in SA)
    private static final Pattern POSTAL_CODE_PATTERN =
        Pattern.compile("^[0-9]{4}$");

    // Password strength: min 8 chars, at least 1 upper, 1 lower, 1 digit, 1 special char
    private static final Pattern STRONG_PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$");

    // Valid South African provinces from database CHECK constraint
    private static final List<String> VALID_PROVINCES = List.of(
        "Eastern Cape", "Free State", "Gauteng", "KwaZulu-Natal",
        "Limpopo", "Mpumalanga", "Northern Cape", "North West", "Western Cape"
    );

    // Valid user roles from expanded database
    private static final List<String> VALID_ROLES = List.of(
        "Admin", "OrphanageRep", "OrphanageStaff", "Donor", "Volunteer", "Staff"
    );

    // Valid resource types
    private static final List<String> VALID_RESOURCE_TYPES = List.of(
        "Food", "Clothing", "Educational", "Medical", "Furniture", 
        "Sports", "Hygiene", "Books", "Electronics", "Toys", "Money", "Other"
    );

    // Valid urgency levels
    private static final List<String> VALID_URGENCY_LEVELS = List.of(
        "Critical", "High", "Medium", "Low"
    );

    // Email validation with Try monad for error handling
    public static final Function1<String, Try<Boolean>> isValidEmail = email ->
        Try.of(() -> email != null && EMAIL_PATTERN.matcher(email.trim()).matches())
           .recover(throwable -> false);

    // Username validation
    public static final Function1<String, Try<Boolean>> isValidUsername = username ->
        Try.of(() -> username != null && USERNAME_PATTERN.matcher(username.trim()).matches())
           .recover(throwable -> false);

    // South African phone number validation
    public static final Function1<String, Try<Boolean>> isValidPhone = phone ->
        Try.of(() -> {
            if (phone == null) return false;
            String cleaned = phone.replaceAll("[\\s-()]", ""); // Remove spaces, dashes, parentheses
            return SA_PHONE_PATTERN.matcher(cleaned).matches();
        }).recover(throwable -> false);

    // South African ID number validation with checksum
    public static final Function1<String, Try<Boolean>> isValidSAIdNumber = idNumber ->
        Try.of(() -> {
            if (idNumber == null || !SA_ID_PATTERN.matcher(idNumber).matches()) {
                return false;
            }
            // Validate date portion (first 6 digits)
            String dateStr = idNumber.substring(0, 6);
            int year = Integer.parseInt(dateStr.substring(0, 2));
            int month = Integer.parseInt(dateStr.substring(2, 4));
            int day = Integer.parseInt(dateStr.substring(4, 6));
            
            // Determine century (00-99 could be 1900s or 2000s)
            year += (year <= LocalDate.now().getYear() % 100) ? 2000 : 1900;
            
            if (month < 1 || month > 12 || day < 1 || day > 31) {
                return false;
            }
            
            // Luhn algorithm for checksum validation
            return validateLuhn(idNumber);
        }).recover(throwable -> false);

    // Province validation
    public static final Function1<String, Try<Boolean>> isValidProvince = province ->
        Try.of(() -> province != null && VALID_PROVINCES.contains(province.trim()))
           .recover(throwable -> false);

    // Role validation
    public static final Function1<String, Try<Boolean>> isValidRole = role ->
        Try.of(() -> role != null && VALID_ROLES.contains(role.trim()))
           .recover(throwable -> false);

    // Registration number validation for orphanages
    public static final Function1<String, Try<Boolean>> isValidRegistrationNumber = regNumber ->
        Try.of(() -> regNumber != null && REGISTRATION_NUMBER_PATTERN.matcher(regNumber.trim()).matches())
           .recover(throwable -> false);

    // Bank account validation
    public static final Function1<String, Try<Boolean>> isValidBankAccount = account ->
        Try.of(() -> account != null && BANK_ACCOUNT_PATTERN.matcher(account.replaceAll("\\s", "")).matches())
           .recover(throwable -> false);

    // Postal code validation
    public static final Function1<String, Try<Boolean>> isValidPostalCode = code ->
        Try.of(() -> code != null && POSTAL_CODE_PATTERN.matcher(code.trim()).matches())
           .recover(throwable -> false);

    // Password strength validation
    public static final Function1<String, Try<Boolean>> isStrongPassword = password ->
        Try.of(() -> password != null && STRONG_PASSWORD_PATTERN.matcher(password).matches())
           .recover(throwable -> false);

    // Resource type validation
    public static final Function1<String, Try<Boolean>> isValidResourceType = type ->
        Try.of(() -> type != null && VALID_RESOURCE_TYPES.contains(type.trim()))
           .recover(throwable -> false);

    // Urgency level validation
    public static final Function1<String, Try<Boolean>> isValidUrgencyLevel = level ->
        Try.of(() -> level != null && VALID_URGENCY_LEVELS.contains(level.trim()))
           .recover(throwable -> false);

    // Amount validation (positive decimal)
    public static final Function1<Double, Try<Boolean>> isValidAmount = amount ->
        Try.of(() -> amount != null && amount > 0 && amount <= 10000000) // Max 10 million
           .recover(throwable -> false);

    // Date validation (not in past for certain contexts)
    public static final Function1<LocalDate, Try<Boolean>> isValidFutureDate = date ->
        Try.of(() -> date != null && !date.isBefore(LocalDate.now()))
           .recover(throwable -> false);

    // Age validation (0-120 years)
    public static final Function1<Integer, Try<Boolean>> isValidAge = age ->
        Try.of(() -> age != null && age >= 0 && age <= 120)
           .recover(throwable -> false);

    // Capacity validation for orphanages
    public static final Function1<Integer, Try<Boolean>> isValidCapacity = capacity ->
        Try.of(() -> capacity != null && capacity > 0 && capacity <= 1000)
           .recover(throwable -> false);

    // Basic non-empty string validation
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    // Check if string is within length limits
    public static boolean isWithinLength(String str, int minLength, int maxLength) {
        if (str == null) return false;
        int length = str.trim().length();
        return length >= minLength && length <= maxLength;
    }

    // Sanitize input to prevent XSS/SQL injection
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.replaceAll("[<>\"'&]", "")
                   .replaceAll("(?i)(script|javascript|onclick|onload)", "")
                   .trim();
    }

    // Validate website URL
    public static boolean isValidWebsite(String url) {
        if (url == null || url.trim().isEmpty()) return true; // Optional field
        try {
            Pattern urlPattern = Pattern.compile(
                "^(https?://)?(www\\.)?[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+(/.*)?$"
            );
            return urlPattern.matcher(url.trim()).matches();
        } catch (Exception e) {
            return false;
        }
    }

    // Helper method for Luhn algorithm (SA ID validation)
    private static boolean validateLuhn(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    // Get list of valid provinces for UI dropdowns
    public static List<String> getValidProvinces() {
        return VALID_PROVINCES;
    }

    // Get list of valid roles for UI dropdowns
    public static List<String> getValidRoles() {
        return VALID_ROLES;
    }

    // Get list of valid resource types for UI dropdowns
    public static List<String> getValidResourceTypes() {
        return VALID_RESOURCE_TYPES;
    }

    // Get list of valid urgency levels for UI dropdowns
    public static List<String> getValidUrgencyLevels() {
        return VALID_URGENCY_LEVELS;
    }
}