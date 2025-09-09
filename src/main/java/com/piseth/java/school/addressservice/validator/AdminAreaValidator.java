package com.piseth.java.school.addressservice.validator;

import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.piseth.java.school.addressservice.domain.AdminArea;
import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;

@Component
public class AdminAreaValidator {
	
	// 2, 4, 6, or 8 digits (e.g., 12, 1201, 120101, 12010101)
    private static final Pattern CODE_PATTERN = Pattern.compile("^\\d{2}(?:\\d{2}){0,3}$");

    public void validate(final AdminArea request) {

        if (Objects.isNull(request)) {
            throw new IllegalArgumentException("request is required");
        }

        if (Objects.isNull(request.getLevel())) {
            throw new IllegalArgumentException("level is required");
        }

        if (Objects.isNull(request.getCode()) || request.getCode().isBlank()) {
            throw new IllegalArgumentException("code is required");
        }

        final String code = request.getCode().trim();

        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException("code must look like 12, 1201, 120101, or 12010101 (no dashes)");
        }

        // each level adds 2 digits
        final int depth = code.length() / 2;
        final int expectedDepth = request.getLevel().depth(); // PROVINCE=1, DISTRICT=2, COMMUNE=3, VILLAGE=4
        if (depth != expectedDepth) {
            throw new IllegalArgumentException("Code depth does not match level: " + request.getLevel());
        }

        if (request.getLevel() == AdminLevel.PROVINCE) {
            if (request.getParentCode() != null && !request.getParentCode().isBlank()) {
                throw new IllegalArgumentException("ParentCode must be null for PROVINCE");
            }
        } else {
            final String parent = request.getParentCode();
            if (Objects.isNull(parent) || parent.isBlank()) {
                throw new IllegalArgumentException("ParentCode is required for: " + request.getLevel());
            }
            // parent must also be dashless and a prefix of child
            if (!CODE_PATTERN.matcher(parent).matches()) {
                throw new IllegalArgumentException("parentCode must be dashless numeric blocks of 2 digits (e.g., 12, 1201, 120101)");
            }
            if (!code.startsWith(parent)) {
                throw new IllegalArgumentException("code must start with parentCode");
            }
        }
    }

}
