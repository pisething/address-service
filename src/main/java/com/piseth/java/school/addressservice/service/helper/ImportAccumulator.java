package com.piseth.java.school.addressservice.service.helper;

import java.util.ArrayList;
import java.util.List;

import com.piseth.java.school.addressservice.dto.RowError;
import com.piseth.java.school.addressservice.dto.RowResult;

import lombok.Getter;

@Getter
public class ImportAccumulator {
	private int totalRows;
	private int inserted;
	private int duplicates;
	private int validationErrors;
	private int parentMissing;
	private int otherErrors;

	private List<RowError> errors = new ArrayList<>();
	
	public ImportAccumulator accumulate(final RowResult r) {
		this.totalRows++;
		
		if(r.isInserted()) {
			this.inserted++;
		}else {
			switch(r.getOutcome()) {
			case DUPLICATE : this.duplicates++; break;
			case VALIDATION : this.validationErrors++; break;
			case PARENT_MISSING : this.parentMissing++; break;
			case OTHER : this.otherErrors++; break;
			}
		}
		
		if(r.getError() != null) {
			errors.add(r.getError());
		}
		
		return this;
	}
	
	
}
