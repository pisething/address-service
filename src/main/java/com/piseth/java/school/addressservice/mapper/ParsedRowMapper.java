package com.piseth.java.school.addressservice.mapper;

import org.mapstruct.Mapper;

import com.piseth.java.school.addressservice.dto.AdminAreaCreateRequest;
import com.piseth.java.school.addressservice.dto.ParsedRow;

@Mapper(componentModel = "spring")
public interface ParsedRowMapper {

	AdminAreaCreateRequest toCreateRequest(ParsedRow row);
}
