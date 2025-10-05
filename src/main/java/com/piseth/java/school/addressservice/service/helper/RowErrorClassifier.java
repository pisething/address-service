package com.piseth.java.school.addressservice.service.helper;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.piseth.java.school.addressservice.domain.enumeration.Outcome;
import com.piseth.java.school.addressservice.exception.ClassifiableError;

import jakarta.validation.ConstraintViolationException;

@Component
public class RowErrorClassifier {
	
	public String safeMessage(final Throwable ex) {
		if(ex == null) {
			return "Unknown Error";
		} else if (ex.getMessage() != null) {
			return ex.getMessage();
		} else {
			return ex.getClass().getSimpleName();
		}
	}
	
	public Outcome classify(final Throwable ex) {
		if(ex == null) {
			return Outcome.OTHER;
		}
		
		if(ex instanceof ClassifiableError classifiableError) {
			return classifiableError.getOutcome();
		}
		
		if(ex instanceof WebExchangeBindException) {
			return Outcome.VALIDATION;
		}
		
		if(ex instanceof ConstraintViolationException) {
			return Outcome.VALIDATION;
		}
		return Outcome.OTHER;
	}

}
