package com.piseth.java.school.addressservice.mapper;

import java.util.ArrayList;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.piseth.java.school.addressservice.domain.AdminArea;
import com.piseth.java.school.addressservice.dto.AdminAreaCreateRequest;
import com.piseth.java.school.addressservice.dto.AdminAreaResponse;
import com.piseth.java.school.addressservice.dto.AdminAreaUpdateRequest;

@Mapper(componentModel = "spring", 
	unmappedTargetPolicy = ReportingPolicy.IGNORE, // don’t complain about fields we don’t map
	nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
	nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE) // skip nulls on update)
public interface AdminAreaMapper {
	
	@Mapping(target = "path", expression = "java(buildPath(dto.getCode()))")
	@Mapping(target = "createAt", ignore = true)
	@Mapping(target = "updateAt", ignore = true)
	@Mapping(target = "version", ignore = true)
	AdminArea toEntity(AdminAreaCreateRequest dto);
	
	
	/**
     * Build cumulative dashless path segments every 2 digits.
     * 12           -> [12]
     * 1201         -> [12, 1201]
     * 120101       -> [12, 1201, 120101]
     * 12010101     -> [12, 1201, 120101, 12010101]
     */
    default List<String> buildPath(final String code) {
        if (code == null || code.isBlank()) {
            return List.of();
        }
        final String trimmed = code.trim();
        final int len = trimmed.length();

        // defensively accept only even lengths up to 8
        if ((len % 2) != 0 || len > 8) {
            return List.of(trimmed); // fallback: at least include the code itself
        }

        final List<String> path = new ArrayList<>(len / 2);
        for (int i = 2; i <= len; i += 2) {
            path.add(trimmed.substring(0, i));
        }
        return path;
    }

	
	AdminAreaResponse toResponse(AdminArea entity);
	
	void update(@MappingTarget AdminArea target, AdminAreaUpdateRequest dto);

}
