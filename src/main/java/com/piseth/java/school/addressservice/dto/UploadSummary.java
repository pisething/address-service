package com.piseth.java.school.addressservice.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadSummary {
	private int totalRows;
	private int inserted;
	private int duplicates;
	private int validationErrors;
	private int parentMissing;
	private int otherErrors;

	private List<RowError> errors;
}
