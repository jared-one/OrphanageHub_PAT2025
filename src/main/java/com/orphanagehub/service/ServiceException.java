package com.orphanagehub.service;

/**
 * Enhanced service layer exception with error codes and categories.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    private final ErrorCode errorCode;
    private final ErrorCategory category;
    private final Object[] params;
    
    /**
     * Error categories for better handling
     */
    public enum ErrorCategory {
        VALIDATION,
        AUTHENTICATION,
        AUTHORIZATION,
        NOT_FOUND,
        CONFLICT,
        BUSINESS_LOGIC,
        EXTERNAL_SERVICE,
        DATABASE,
        SYSTEM
    }
    
    /**
     * Error codes for specific errors
     */
    public enum ErrorCode {
        // Validation errors (1000-1999)
        INVALID_INPUT(1000, "Invalid input: {0}"),
        REQUIRED_FIELD(1001, "Required field missing: {0}"),
        INVALID_FORMAT(1002, "Invalid format for: {0}"),
        OUT_OF_RANGE(1003, "{0} is out of valid range"),
        
        // Authentication errors (2000-2999)
        INVALID_CREDENTIALS(2000, "Invalid username or password"),
        ACCOUNT_LOCKED(2001, "Account is locked"),
        ACCOUNT_SUSPENDED(2002, "Account is suspended"),
        EMAIL_NOT_VERIFIED(2003, "Email address not verified"),
        TOKEN_EXPIRED(2004, "Token has expired"),
        INVALID_TOKEN(2005, "Invalid token"),
        
        // Authorization errors (3000-3999)
        UNAUTHORIZED(3000, "Unauthorized access"),
        INSUFFICIENT_PRIVILEGES(3001, "Insufficient privileges"),
        ROLE_REQUIRED(3002, "Role required: {0}"),
        
        // Not found errors (4000-4999)
        USER_NOT_FOUND(4000, "User not found"),
        ORPHANAGE_NOT_FOUND(4001, "Orphanage not found"),
        RESOURCE_NOT_FOUND(4002, "Resource not found"),
        DONATION_NOT_FOUND(4003, "Donation not found"),
        
        // Conflict errors (5000-5999)
        DUPLICATE_USERNAME(5000, "Username already exists"),
        DUPLICATE_EMAIL(5001, "Email already registered"),
        ALREADY_EXISTS(5002, "{0} already exists"),
        ALREADY_APPLIED(5003, "Already applied"),
        
        // Business logic errors (6000-6999)
        INVALID_STATE(6000, "Invalid state for operation"),
        LIMIT_EXCEEDED(6001, "Limit exceeded: {0}"),
        NOT_ELIGIBLE(6002, "Not eligible for: {0}"),
        OPERATION_FAILED(6003, "Operation failed: {0}"),
        
        // External service errors (7000-7999)
        PAYMENT_FAILED(7000, "Payment processing failed"),
        EMAIL_SEND_FAILED(7001, "Failed to send email"),
        SMS_SEND_FAILED(7002, "Failed to send SMS"),
        API_ERROR(7003, "External API error: {0}"),
        
        // Database errors (8000-8999)
        DATABASE_ERROR(8000, "Database operation failed"),
        CONNECTION_ERROR(8001, "Database connection failed"),
        TRANSACTION_FAILED(8002, "Transaction failed"),
        
        // System errors (9000-9999)
        INTERNAL_ERROR(9000, "Internal server error"),
        CONFIGURATION_ERROR(9001, "Configuration error: {0}"),
        INITIALIZATION_ERROR(9002, "Initialization failed: {0}");
        
        private final int code;
        private final String messageTemplate;
        
        ErrorCode(int code, String messageTemplate) {
            this.code = code;
            this.messageTemplate = messageTemplate;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getMessageTemplate() {
            return messageTemplate;
        }
    }
    
    // Constructors
    
    public ServiceException(String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
        this.category = ErrorCategory.SYSTEM;
        this.params = new Object[0];
    }
    
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
        this.category = ErrorCategory.SYSTEM;
        this.params = new Object[0];
    }
    
    public ServiceException(ErrorCode errorCode, Object... params) {
        super(formatMessage(errorCode, params));
        this.errorCode = errorCode;
        this.category = determineCategory(errorCode);
        this.params = params;
    }
    
    public ServiceException(ErrorCode errorCode, Throwable cause, Object... params) {
        super(formatMessage(errorCode, params), cause);
        this.errorCode = errorCode;
        this.category = determineCategory(errorCode);
        this.params = params;
    }
    
    public ServiceException(ErrorCode errorCode, ErrorCategory category, Object... params) {
        super(formatMessage(errorCode, params));
        this.errorCode = errorCode;
        this.category = category;
        this.params = params;
    }
    
    // Getters
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public ErrorCategory getCategory() {
        return category;
    }
    
    public Object[] getParams() {
        return params;
    }
    
    public int getNumericCode() {
        return errorCode.getCode();
    }
    
    // Helper methods
    
    private static String formatMessage(ErrorCode errorCode, Object... params) {
        String template = errorCode.getMessageTemplate();
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                template = template.replace("{" + i + "}", 
                    params[i] != null ? params[i].toString() : "null");
            }
        }
        return template;
    }
    
    private static ErrorCategory determineCategory(ErrorCode errorCode) {
        int code = errorCode.getCode();
        if (code >= 1000 && code < 2000) return ErrorCategory.VALIDATION;
        if (code >= 2000 && code < 3000) return ErrorCategory.AUTHENTICATION;
        if (code >= 3000 && code < 4000) return ErrorCategory.AUTHORIZATION;
        if (code >= 4000 && code < 5000) return ErrorCategory.NOT_FOUND;
        if (code >= 5000 && code < 6000) return ErrorCategory.CONFLICT;
        if (code >= 6000 && code < 7000) return ErrorCategory.BUSINESS_LOGIC;
        if (code >= 7000 && code < 8000) return ErrorCategory.EXTERNAL_SERVICE;
        if (code >= 8000 && code < 9000) return ErrorCategory.DATABASE;
        return ErrorCategory.SYSTEM;
    }
    
    @Override
    public String toString() {
        return String.format("ServiceException[code=%d, category=%s, message=%s]",
            errorCode.getCode(), category, getMessage());
    }
}