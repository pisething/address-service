package com.piseth.java.school.addressservice.exception;

import com.piseth.java.school.addressservice.domain.enumeration.Outcome;

public class ParentNotFoundException extends AdminAreaException{

	public ParentNotFoundException(final String parentCode) {
		super(Outcome.PARENT_MISSING, "Parent not found: " + parentCode);
	}

}