package com.smartporter.exception;

public class SmartPorterException extends RuntimeException {
    public SmartPorterException(String message) {
        super(message);
    }
    public SmartPorterException(String message, Throwable cause) {
        super(message, cause);
    }
}
