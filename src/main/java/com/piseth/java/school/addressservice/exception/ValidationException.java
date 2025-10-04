package com.piseth.java.school.addressservice.exception;

import com.piseth.java.school.addressservice.domain.enumeration.Outcome;

public class ValidationException extends AdminAreaException {
    public ValidationException(final String message) {
        super(Outcome.VALIDATION,message);
    }
}
