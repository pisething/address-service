package com.piseth.java.school.addressservice.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.piseth.java.school.addressservice.dto.ImportCounters;
import com.piseth.java.school.addressservice.dto.RowError;
import com.piseth.java.school.addressservice.dto.UploadSummary;

@Mapper(componentModel = "spring")
public interface AdminAreaImportMapper {

    @Mapping(target = "totalRows",        source = "c.totalRows")
    @Mapping(target = "inserted",         source = "c.inserted")
    @Mapping(target = "duplicates",       source = "c.duplicates")
    @Mapping(target = "validationErrors", source = "c.validationErrors")
    @Mapping(target = "parentMissing",    source = "c.parentMissing")
    @Mapping(target = "otherErrors",      source = "c.otherErrors")
    @Mapping(target = "errors",           source = "errors")
    UploadSummary toSummary(ImportCounters c, List<RowError> errors);
}
