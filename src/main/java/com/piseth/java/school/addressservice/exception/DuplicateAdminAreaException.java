package com.piseth.java.school.addressservice.exception;

import lombok.Getter;

@Getter
public class DuplicateAdminAreaException extends AdminAreaException{

	private String code;
	public DuplicateAdminAreaException(String code) {
		super("AdminArea already exists: " + code);
		this.code = code;
	}
	
}
