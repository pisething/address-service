package com.piseth.java.school.addressservice.exception;

public class AdminAreaNotFoundException extends AdminAreaException{

	public AdminAreaNotFoundException(String code) {
		super("Admin Area not found : " + code);
	}

}
