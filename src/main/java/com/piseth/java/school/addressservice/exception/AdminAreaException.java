package com.piseth.java.school.addressservice.exception;

import com.piseth.java.school.addressservice.domain.enumeration.Outcome;

public class AdminAreaException extends RuntimeException implements ClassifiableError{
	
	private static final long serialVersionUID = -6319905709220563415L;
	
	private final Outcome outcome;

	public AdminAreaException(final Outcome outcome, final String message) {
		super(message);
		this.outcome = outcome;
	}
	
	public AdminAreaException(final Outcome outcome, final String message, final Throwable cause) {
		super(message, cause);
		this.outcome = outcome;
	}

	@Override
	public Outcome getOutcome() {
		// TODO Auto-generated method stub
		return this.outcome;
	}

}
