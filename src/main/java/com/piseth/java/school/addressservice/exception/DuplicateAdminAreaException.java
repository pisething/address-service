package com.piseth.java.school.addressservice.exception;

import com.piseth.java.school.addressservice.domain.enumeration.Outcome;

public class DuplicateAdminAreaException extends AdminAreaException {
    private final String code;
    public DuplicateAdminAreaException(final String code) {
        super(Outcome.DUPLICATE, "AdminArea already exists: " + code);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}
