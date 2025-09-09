package com.piseth.java.school.addressservice.util;

import java.util.ArrayList;
import java.util.List;

import com.piseth.java.school.addressservice.dto.ImportCounters;
import com.piseth.java.school.addressservice.dto.RowError;
import com.piseth.java.school.addressservice.dto.RowResult;
import com.piseth.java.school.addressservice.dto.UploadSummary;
import com.piseth.java.school.addressservice.mapper.AdminAreaImportMapper;

public final class ImportAccumulator {

    int totalRows;
    int inserted;
    int duplicates;
    int validationErrors;
    int parentMissing;
    int otherErrors;
    final List<RowError> errors = new ArrayList<>();

    public ImportAccumulator accumulate(final RowResult r) {
        this.totalRows++;
        if (r.isInserted()) {
            this.inserted++;
        } else {
            switch (r.outcome()) {
                case DUPLICATE: {
                    this.duplicates++;
                    break;
                }
                case VALIDATION: {
                    this.validationErrors++;
                    break;
                }
                case PARENT_MISSING: {
                    this.parentMissing++;
                    break;
                }
                case OTHER: {
                    this.otherErrors++;
                    break;
                }
                default: {
                    break;
                }
            }
            if (r.errorOrNull() != null) {
                this.errors.add(r.errorOrNull());
            }
        }
        return this;
    }

    public ImportCounters toCounters() {
        return new ImportCounters(
            this.totalRows,
            this.inserted,
            this.duplicates,
            this.validationErrors,
            this.parentMissing,
            this.otherErrors
        );
    }
    
    public UploadSummary toSummary(final AdminAreaImportMapper mapper) {
        // pass a defensive copy if you want immutability
        return mapper.toSummary(this.toCounters(), new ArrayList<>(this.errors));
    }
}
