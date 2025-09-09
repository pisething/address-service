package com.piseth.java.school.addressservice.dto;

import com.piseth.java.school.addressservice.domain.enumeration.Outcome;

public final class RowResult {

    private final Outcome outcome;
    private final RowError errorOrNull;

    private RowResult(final Outcome outcome, final RowError errorOrNull) {
        this.outcome = outcome;
        this.errorOrNull = errorOrNull;
    }

    public static RowResult inserted() {
        return new RowResult(Outcome.INSERTED, null);
    }

    public static RowResult error(final Outcome outcome, final RowError error) {
        return new RowResult(outcome, error);
    }

    public Outcome outcome() {
        return this.outcome;
    }

    public RowError errorOrNull() {
        return this.errorOrNull;
    }

    public boolean isInserted() {
        return this.outcome == Outcome.INSERTED;
    }
}
