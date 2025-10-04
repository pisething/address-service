package com.piseth.java.school.addressservice.exception;

import com.piseth.java.school.addressservice.domain.enumeration.Outcome;

public class AdminAreaNotFoundException extends AdminAreaException {
    public AdminAreaNotFoundException(String code) { super(Outcome.OTHER, "AdminArea not found: " + code); }
}
