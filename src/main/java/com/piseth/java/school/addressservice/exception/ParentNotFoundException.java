package com.piseth.java.school.addressservice.exception;

public class ParentNotFoundException extends AdminAreaException {
    private final String parentCode;
    public ParentNotFoundException(final String parentCode) {
        super("Parent not found: " + parentCode);
        this.parentCode = parentCode;
    }
    public String getParentCode() {
        return parentCode;
    }
}
