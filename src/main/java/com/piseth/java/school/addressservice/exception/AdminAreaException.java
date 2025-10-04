package com.piseth.java.school.addressservice.exception;

import com.piseth.java.school.addressservice.domain.enumeration.Outcome;

public class AdminAreaException extends RuntimeException implements ClassifiableError {

  private final Outcome outcome;

  protected AdminAreaException(final Outcome outcome, final String message) {
    super(message);
    this.outcome = outcome;
  }

  protected AdminAreaException(final Outcome outcome, final String message, final Throwable cause) {
    super(message, cause);
    this.outcome = outcome;
  }

  @Override
  public Outcome getOutcome() {
    return outcome;
  }
}
