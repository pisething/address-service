package com.piseth.java.school.addressservice.dto;

public record ImportCounters(
    int totalRows,
    int inserted,
    int duplicates,
    int validationErrors,
    int parentMissing,
    int otherErrors
) {}
