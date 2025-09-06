/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.service;

public class ServiceException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
