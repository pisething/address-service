package com.piseth.java.school.addressservice.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class UploadSummary {
    private int totalRows;
    private int inserted;
    private int duplicates;
    private int validationErrors;
    private int parentMissing;
    private int otherErrors;

    private List<RowError> errors = new ArrayList<>();
}
