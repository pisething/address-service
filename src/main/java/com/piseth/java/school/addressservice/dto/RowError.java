package com.piseth.java.school.addressservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RowError {
	private int line;
	private String code;
	private String message;
}
