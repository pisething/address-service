package com.piseth.java.school.addressservice.exception;

public class ChildrenExistException extends AdminAreaException{

	public ChildrenExistException(String code) {
		super("Has Children: Cannot delete Admin Area: " + code);
	}

}
