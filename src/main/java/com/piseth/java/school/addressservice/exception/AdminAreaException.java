package com.piseth.java.school.addressservice.exception;

public class AdminAreaException extends RuntimeException {
    public AdminAreaException(final String message) {
        super(message);
    }
    public AdminAreaException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
