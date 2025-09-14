package com.piseth.java.school.addressservice.exception;

public class ChildrenExistException extends AdminAreaException {
    public ChildrenExistException(String code) { super("Cannot delete: children exist for " + code); }
}
