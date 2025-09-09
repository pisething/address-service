package com.piseth.java.school.addressservice.web.upload;

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
	// Accept 2/4/6/8 digits, no dashes/spaces.
    private static final Pattern CODE_PATTERN = Pattern.compile("^\\d{2}(?:\\d{2}){0,3}$");

    private String trimmedCode() {
        if (code == null) {
            return "";
        } else {
            return code.trim();
        }
    }

    /** Depth by 2-digit blocks; invalid codes sort last. */
    public int depth() {
        final String c = trimmedCode();
        if (c.isEmpty() || !CODE_PATTERN.matcher(c).matches()) {
            return Integer.MAX_VALUE;
        } else {
            return c.length() / 2; // PROVINCE=1, DISTRICT=2, COMMUNE=3, VILLAGE=4
        }
    }

    /** Parents first, then code, then original line for stability. */
    public static final Comparator<ParsedRow> BY_DEPTH =
        Comparator.comparingInt(ParsedRow::depth)
                  .thenComparing(ParsedRow::trimmedCode)
                  .thenComparingInt(ParsedRow::lineNumber);
}
