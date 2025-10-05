package com.piseth.java.school.addressservice.exception;

import com.piseth.java.school.addressservice.domain.enumeration.Outcome;

public interface ClassifiableError {

	Outcome getOutcome();
}
