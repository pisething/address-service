package com.piseth.java.school.addressservice.exception;

public class AdminAreaException extends RuntimeException{
	
	private static final long serialVersionUID = -6319905709220563415L;

	public AdminAreaException(final String message) {
		super(message);
	}
	
	public AdminAreaException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
