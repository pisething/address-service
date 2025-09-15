package com.piseth.java.school.addressservice.exception;

public class ParentNotFoundException extends AdminAreaException{

	public ParentNotFoundException(final String parentCode) {
		super("Parent not found: " + parentCode);
	}

}