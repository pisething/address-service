package com.piseth.java.school.addressservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.piseth.java.school.addressservice.dto.AdminAreaCreateRequest;
import com.piseth.java.school.addressservice.web.upload.ParsedRow;

@Mapper(componentModel = "spring")
public interface ParsedRowMapper {

    @Mapping(target = "code",       source = "code")
    @Mapping(target = "level",      source = "level")
    @Mapping(target = "parentCode", source = "parentCode")
    @Mapping(target = "nameKh",     source = "nameKh")
    @Mapping(target = "nameEn",     source = "nameEn")
    AdminAreaCreateRequest toCreateRequest(ParsedRow row);
}
