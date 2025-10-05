package com.piseth.java.school.addressservice.mapper;

import org.mapstruct.Mapper;

import com.piseth.java.school.addressservice.dto.UploadSummary;
import com.piseth.java.school.addressservice.service.helper.ImportAccumulator;

@Mapper(componentModel = "spring")
public interface UploadSummaryMapper {
	UploadSummary toUploadSummary(ImportAccumulator accumulator);
}
