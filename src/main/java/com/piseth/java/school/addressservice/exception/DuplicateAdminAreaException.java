package com.piseth.java.school.addressservice.exception;

public class DuplicateAdminAreaException extends AdminAreaException {
    private final String code;
    public DuplicateAdminAreaException(final String code) {
        super("AdminArea already exists: " + code);
        this.code = code;
    }
    public String getCode() {
        return code;
    }
}
