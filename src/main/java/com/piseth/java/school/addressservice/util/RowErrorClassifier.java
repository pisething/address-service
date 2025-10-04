package com.piseth.java.school.addressservice.util;

import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import com.piseth.java.school.addressservice.domain.enumeration.Outcome;
import com.piseth.java.school.addressservice.exception.ClassifiableError;
import jakarta.validation.ConstraintViolationException;

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

public Outcome classify(final Throwable ex) {
    if (ex == null) {
        return Outcome.OTHER;
    }

    // If the exception declares its own outcome, trust it.
    if (ex instanceof ClassifiableError classifiable) {
        return Objects.requireNonNullElse(classifiable.getOutcome(), Outcome.OTHER);
    }

    // Common framework-level validation buckets (no string matching).
    if (ex instanceof WebExchangeBindException) {
        return Outcome.VALIDATION;
    }
    if (ex instanceof ConstraintViolationException) {
        return Outcome.VALIDATION;
    }
    if (ex instanceof ServerWebInputException) {
        return Outcome.VALIDATION;
    }

    return Outcome.OTHER;
}
}
