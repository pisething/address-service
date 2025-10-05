package com.piseth.java.school.addressservice.exception;

import com.piseth.java.school.addressservice.domain.enumeration.Outcome;

import lombok.Getter;

@Getter
public class DuplicateAdminAreaException extends AdminAreaException{

	private String code;
	public DuplicateAdminAreaException(String code) {
		super(Outcome.DUPLICATE, "AdminArea already exists: " + code);
		this.code = code;
	}
	
}
