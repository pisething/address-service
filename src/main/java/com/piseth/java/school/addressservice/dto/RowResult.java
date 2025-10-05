package com.piseth.java.school.addressservice.dto;

import com.piseth.java.school.addressservice.domain.enumeration.Outcome;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class RowResult {

	private final Outcome outcome;
	private final RowError error;
	
	public static RowResult inserted() {
		return new RowResult(Outcome.INSERTED, null);
	}
	
	public static RowResult error(final Outcome outcome, final RowError error) {
		return new RowResult(outcome, error);
	}
	
	public boolean isInserted() {
		return this.outcome == Outcome.INSERTED;
	}
	
}
