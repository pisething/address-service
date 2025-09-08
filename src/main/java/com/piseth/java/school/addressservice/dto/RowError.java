package com.piseth.java.school.addressservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RowError {
	private int line; // 1-based including header
	private String code;
	private String message;
}
