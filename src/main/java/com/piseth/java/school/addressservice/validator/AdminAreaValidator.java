package com.piseth.java.school.addressservice.validator;

import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.piseth.java.school.addressservice.domain.AdminArea;
import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;

@Component
public class AdminAreaValidator {
	
	private static final Pattern CODE_PATTERN = Pattern.compile("^[0-9]{2}(?:-[0-9]{2}){0,3}$");
	
	public void validate(AdminArea request) {
		
		if(Objects.isNull(request)) {
			throw new IllegalArgumentException("request is required");
		}
		
		if(Objects.isNull(request.getLevel())) {
			throw new IllegalArgumentException("level is required");
		}
		
		if(Objects.isNull(request.getCode()) || request.getCode().isBlank()) {
			throw new IllegalArgumentException("code is required");
		}
		
		
		if(!CODE_PATTERN.matcher(request.getCode()).matches()) {
			throw new IllegalArgumentException("code must look like 12 or 12-03-09-02");
		}
		
		
		final int depth = request.getCode().split("-").length;
		final int expectedDepth = request.getLevel().depth();
		if(depth != expectedDepth) {
			throw new IllegalArgumentException("Code depth does not match level : " + request.getLevel());
		}
		
		if(request.getLevel() == AdminLevel.PROVINCE) {
			if(request.getParentCode() != null) {
				throw new IllegalArgumentException("ParentCode must be null for PROVINCE");
			}
		}else {
			if(Objects.isNull(request.getParentCode()) || request.getParentCode().isBlank()) {
				throw new IllegalArgumentException("ParentCode is required for : " + request.getLevel());
			}
			
			if(!request.getCode().startsWith(request.getParentCode())) {
				throw new IllegalArgumentException("code must start with parentCode");
			}
		}
		
		
		
	}

}
