package com.piseth.java.school.addressservice.util;

import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;

import java.util.Comparator;

public record ParsedRow(
    int lineNumber,
    String code,
    AdminLevel level,
    String parentCode,
    String nameKh,
    String nameEn
) {
    public int depth() {
        if (code == null || code.isBlank()) {
            return Integer.MAX_VALUE;
        } else {
            return code.split("-").length;
        }
    }

    public static final Comparator<ParsedRow> BY_DEPTH = Comparator.comparingInt(ParsedRow::depth);
}
