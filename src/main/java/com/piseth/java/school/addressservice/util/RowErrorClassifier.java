package com.piseth.java.school.addressservice.util;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.piseth.java.school.addressservice.domain.enumeration.Outcome;

@Component
public class RowErrorClassifier {

    public String safeMessage(final Throwable ex) {
        if (ex == null) {
            return "Unknown error";
        } else if (ex.getMessage() != null) {
            return ex.getMessage();
        } else {
            return ex.getClass().getSimpleName();
        }
    }

    public Outcome classify(final String message) {
        if (message == null) {
            return Outcome.OTHER;
        }

        if (message.startsWith("AdminArea already exists")) {
            return Outcome.DUPLICATE;
        }

        if (message.startsWith("Parent not found")) {
            return Outcome.PARENT_MISSING;
        }

        final String lower = message.toLowerCase(Locale.ROOT);
        if (lower.contains("required")) {
            return Outcome.VALIDATION;
        } else if (lower.contains("depth")) {
            return Outcome.VALIDATION;
        } else if (lower.contains("look like")) {
            return Outcome.VALIDATION;
        } else if (lower.contains("unknown level")) {
            return Outcome.VALIDATION;
        } else {
            return Outcome.OTHER;
        }
    }
}
