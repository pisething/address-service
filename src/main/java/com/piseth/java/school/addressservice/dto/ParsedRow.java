package com.piseth.java.school.addressservice.dto;

import java.util.Comparator;
import java.util.regex.Pattern;

import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;

public record ParsedRow(
		int lineNumber, 
		String code, 
		AdminLevel level,
		String parentCode, 
		String nameKh, 
		String nameEn

) {
	private static final Pattern CODE_PATTERN = Pattern.compile("^\\d{2}(?:\\d{2}){0,3}$");
	private String trimmedCode() {
		if(code == null) {
			return "";
		}
		return code.trim();
	}
	
	public int depth() {
		final String c = trimmedCode();
		if(c.isEmpty() || !CODE_PATTERN.matcher(c).matches()) {
			return Integer.MAX_VALUE;
		}
		return c.length() / 2;
	}
	
	public static final Comparator<ParsedRow> BY_DEPTH = 
			Comparator.comparingInt(ParsedRow::depth)
				.thenComparing(ParsedRow::trimmedCode)
				.thenComparing(ParsedRow::lineNumber);

}
