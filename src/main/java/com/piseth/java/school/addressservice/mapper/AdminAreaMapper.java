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
	unmappedTargetPolicy = ReportingPolicy.IGNORE,
	nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
	nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
		)
public interface AdminAreaMapper {
	
	@Mapping(target = "path", expression = "java(buildPath(dto.getCode()))")
	AdminArea toEntity(AdminAreaCreateRequest dto);
	
	/*
	 
	 12 -> [12]
	 1201 ->[12,1201]
	 120101 -> [12,1201,120101]
	 12010101 -> [12,1201,120101, 12010101]
	 
	  12    
	 * */
	
	default List<String> buildPath(String code){
		if( code == null || code.isBlank()) {
			return List.of();
		}
		
		final String trimmed = code.trim();
		final int len = trimmed.length();
		
		if((len%2 != 0) || len > 8) {
			return List.of(trimmed);
		}
		
		List<String> path = new ArrayList<>(len / 2);
		for(int i = 2; i <= len; i += 2) {
			path.add(trimmed.substring(0, i));
		}
		
		return path;
	}
	
	AdminAreaResponse toResponse(AdminArea entity);
	
	void update(@MappingTarget AdminArea target, AdminAreaUpdateRequest dto);

}
