package com.piseth.java.school.addressservice.validator;

import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.piseth.java.school.addressservice.domain.AdminArea;
import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;
import com.piseth.java.school.addressservice.exception.ValidationException;

@Component
public class AdminAreaValidator {
	// 2, 4, 6, 0r 8 digit (ex: 12, 1201, 120101, 12010101)
	private static final Pattern CODE_PATTERN = Pattern.compile("^\\d{2}(?:\\d{2}){0,3}$");
	
	public void validate(AdminArea request) {
		
		if(Objects.isNull(request)) {
			throw new ValidationException("request is required");
		}
		
		if(Objects.isNull(request.getLevel())) {
			throw new ValidationException("level is required");
		}
		
		if(Objects.isNull(request.getCode()) || request.getCode().isBlank()) {
			throw new ValidationException("code is required");
		}
		
		
		if(!CODE_PATTERN.matcher(request.getCode()).matches()) {
			throw new ValidationException("code must look like 12 or 12030902");
		}
		
		final String code = request.getCode().trim();
		
		final int depth = code.length() / 2;
		final int expectedDepth = request.getLevel().depth();
		
		if(depth != expectedDepth) {
			throw new ValidationException("Code depth does not match level : " + request.getLevel());
		}
		
		if(request.getLevel() == AdminLevel.PROVINCE) {
			if(request.getParentCode() != null) {
				throw new ValidationException("ParentCode must be null for PROVINCE");
			}
		}else {
			String parentCode = request.getParentCode();
			
			if(Objects.isNull(parentCode) || parentCode.isBlank()) {
				throw new IllegalArgumentException("ParentCode is required for : " + request.getLevel());
			}
			
			if(!CODE_PATTERN.matcher(parentCode).matches()) {
				throw new ValidationException("parentCode must look like 12 or 120309");
			}
			
			if(!request.getCode().startsWith(parentCode)) {
				throw new IllegalArgumentException("code must start with parentCode");
			}
		}
		
		
		
	}

}
